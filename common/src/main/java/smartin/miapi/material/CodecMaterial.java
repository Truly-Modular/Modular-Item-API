package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import smartin.miapi.Miapi;
import smartin.miapi.craft.IngredientWithCount;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.palette.FallbackColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.awt.*;
import java.util.List;
import java.util.*;

public class CodecMaterial implements Material {
    ResourceLocation id;
    Optional<JsonElement> iconJson;
    Optional<JsonElement> paletteJson;
    Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
    Map<String, Map<ModuleProperty<?>, Object>> displayPropertyMap = new HashMap<>();
    List<String> groups;
    List<String> guiGroups;
    List<String> textureKeys;
    Optional<TagKey<Block>> incorrectForTool = Optional.empty();
    Optional<Integer> color = Optional.empty();
    List<IngredientWithCount> items;
    Optional<Boolean> generateConverters;
    Map<String, String> stringData = new HashMap<>();
    Map<String, Double> doubleMap = new HashMap<>();
    public Optional<Component> translation = Optional.empty();
    @Environment(EnvType.CLIENT)
    public MaterialIcons.MaterialIcon icon;
    @Environment(EnvType.CLIENT)
    protected MaterialRenderController palette;

    public static final Codec<CodecMaterial> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CodecMaterial, T>> decode(DynamicOps<T> ops, T input) {
            Map<String, String> stringData = new HashMap<>();
            Map<String, Double> doubleMap = new HashMap<>();
            ops.convertTo(JsonOps.INSTANCE, input).getAsJsonObject().asMap().forEach((string, element) -> {
                if (element.isJsonPrimitive()) {
                    try {
                        stringData.put(string, element.getAsString());
                    } catch (Exception ignored) {
                    }
                    try {
                        doubleMap.put(string, element.getAsDouble());
                    } catch (Exception ignored) {
                    }
                }
            });
            var dataResult = INNER_CODEC.decode(ops, input);
            if (dataResult.isSuccess()) {
                dataResult.getOrThrow().getFirst().setData(stringData, doubleMap);
            }
            return dataResult;
        }

        @Override
        public <T> DataResult<T> encode(CodecMaterial input, DynamicOps<T> ops, T prefix) {
            Codec<Map<String, String>> stringCodec = Codec.unboundedMap(Codec.STRING, Codec.STRING);
            prefix = stringCodec.encode(input.stringData, ops, prefix).result().get();
            Codec<Map<String, Double>> doubleCodec = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE);
            prefix = doubleCodec.encode(input.doubleMap, ops, prefix).result().get();
            return INNER_CODEC.encode(input, ops, prefix);
        }
    };

    public static final Codec<CodecMaterial> INNER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StatResolver.Codecs.JSONELEMENT_CODEC.optionalFieldOf("icon").forGetter(material -> material.iconJson),
            StatResolver.Codecs.JSONELEMENT_CODEC.optionalFieldOf("color_palette").forGetter(material -> material.paletteJson),
            Codec.STRING.listOf().optionalFieldOf("groups", new ArrayList<>()).forGetter(CodecMaterial::getGroups),
            Codec.STRING.listOf().optionalFieldOf("hidden_groups", new ArrayList<>()).forGetter(CodecMaterial::getGuiGroups),
            Codec.STRING.listOf().optionalFieldOf("gui_groups", new ArrayList<>()).forGetter(CodecMaterial::getGuiGroups),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("properties", new HashMap<>()).forGetter(m -> Material.toJsonMap(m.getActualProperty())),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("display_properties", new HashMap<>()).forGetter(m -> Material.toJsonMap(m.getDisplayProperty())),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("hidden_properties", new HashMap<>()).forGetter(m -> Material.toJsonMap(m.getHiddenProperty())),
            Codec.STRING.listOf().optionalFieldOf("textures", List.of("default")).forGetter(CodecMaterial::getTextureKeys),
            ResourceLocation.CODEC.optionalFieldOf("mining_level").forGetter(material -> Optional.of(material.getIncorrectBlocksForDrops().location())),
            Codec.STRING.optionalFieldOf("color").forGetter(m -> Optional.of(Long.toHexString(m.getColor()))),
            IngredientWithCount.CODEC.listOf().optionalFieldOf("items", new ArrayList<>()).forGetter(material -> material.items),
            Miapi.FIXED_BOOL_CODEC.optionalFieldOf("generate_converters").forGetter(m -> Optional.of(m.generateConverters()))
    ).apply(instance, CodecMaterial::new));

    private CodecMaterial(Optional<JsonElement> iconJson,
                          Optional<JsonElement> paletteJson,
                          List<String> groups,
                          List<String> hiddenGroups,
                          List<String> guiGroups,
                          Map<String, JsonElement> property,
                          Map<String, JsonElement> visualProperty,
                          Map<String, JsonElement> hiddenProperty,
                          List<String> textureKeys,
                          Optional<ResourceLocation> incorrectForToolId,
                          Optional<String> color,
                          List<IngredientWithCount> items,
                          Optional<Boolean> generateConverters) {
        this.iconJson = iconJson;
        this.paletteJson = paletteJson;
        this.groups = new ArrayList<>(groups);
        this.guiGroups = new ArrayList<>(guiGroups);
        this.guiGroups.addAll(groups);
        this.groups.addAll(hiddenGroups);
        this.textureKeys = textureKeys;
        var found = BuiltInRegistries.BLOCK.getTags().filter(pair -> pair.getFirst().location().equals(incorrectForToolId)).findAny();
        found.ifPresent(tagKeyNamedPair -> incorrectForTool = Optional.of(tagKeyNamedPair.getFirst()));
        if (color.isPresent()) {
            long longValue = Long.parseLong(color.get(), 16);
            this.color = Optional.of((int) (longValue & 0xffffffffL));
        }
        this.items = items;
        this.generateConverters = generateConverters;
        hiddenProperty.forEach((type, json) -> {
            propertyMap.put(type, ModuleDataPropertiesManager.resolvePropertiesFromJson(json));
        });
        property.forEach((type, json) -> {
            propertyMap.put(type, ModuleDataPropertiesManager.resolvePropertiesFromJson(json));
            displayPropertyMap.put(type, ModuleDataPropertiesManager.resolvePropertiesFromJson(json));
        });
        visualProperty.forEach((type, json) -> {
            displayPropertyMap.put(type, ModuleDataPropertiesManager.resolvePropertiesFromJson(json));
        });
        if (smartin.miapi.Environment.isClient()) {
            if (iconJson.isPresent()) {
                if (iconJson.get() instanceof JsonPrimitive primitive && primitive.isString())
                    icon = new MaterialIcons.TextureMaterialIcon(ResourceLocation.parse(primitive.getAsString()));
                else icon = MaterialIcons.getMaterialIcon(this.id, iconJson.get());
            }
            if (paletteJson.isPresent()) {
                palette = MaterialRenderControllers.creators.get(paletteJson.get().getAsJsonObject().get("type").getAsString()).createPalette(paletteJson.get(), this);
                if (this.color.isEmpty()) {
                    this.color = Optional.of(palette.getAverageColor().argb());
                }
            }
        }
    }

    public CodecMaterial copy() {
        CodecMaterial copy = new CodecMaterial(
                this.iconJson,
                this.paletteJson,
                new ArrayList<>(this.groups),
                List.of(),
                new ArrayList<>(this.guiGroups),
                Material.toJsonMap(this.getActualProperty()),
                Material.toJsonMap(this.getDisplayProperty()),
                Material.toJsonMap(this.getHiddenProperty()),
                new ArrayList<>(this.textureKeys),
                this.incorrectForTool.map(TagKey::location),
                this.color.map(Integer::toHexString),
                new ArrayList<>(this.items),
                this.generateConverters
        );

        // Copy non-constructor fields
        copy.id = this.id;
        copy.stringData = new HashMap<>(this.stringData);
        copy.doubleMap = new HashMap<>(this.doubleMap);
        copy.propertyMap = new HashMap<>(this.propertyMap);
        copy.displayPropertyMap = new HashMap<>(this.displayPropertyMap);
        copy.incorrectForTool = this.incorrectForTool;
        copy.translation = this.translation;

        copy.iconJson = this.iconJson;
        copy.paletteJson = this.paletteJson;
        if (smartin.miapi.Environment.isClient()) {
            if (iconJson.isPresent()) {
                if (iconJson.get() instanceof JsonPrimitive primitive && primitive.isString())
                    copy.icon = new MaterialIcons.TextureMaterialIcon(ResourceLocation.parse(primitive.getAsString()));
                else copy.icon = MaterialIcons.getMaterialIcon(this.id, iconJson.get());
            }
            if (copy.paletteJson.isPresent()) {
                copy.palette = MaterialRenderControllers.creators.get(copy.paletteJson.get().getAsJsonObject().get("type").getAsString()).createPalette(copy.paletteJson.get(), this);
                if (copy.color.isEmpty()) {
                    copy.color = Optional.of(palette.getAverageColor().argb());
                }
            }
        }

        return copy;
    }

    public void merge(CodecMaterial material) {
        if (material.iconJson.isPresent()) {
            this.iconJson = material.iconJson;
        }
        if (material.paletteJson.isPresent()) {
            this.paletteJson = material.paletteJson;
        }

        // Merge groups and guiGroups
        this.groups = new ArrayList<>(this.groups);
        this.groups.addAll(material.groups);
        this.guiGroups = new ArrayList<>(this.guiGroups);
        this.guiGroups.addAll(material.guiGroups);

        // Merge properties
        mergeProperties(material.propertyMap, this.propertyMap);
        mergeProperties(material.displayPropertyMap, this.displayPropertyMap);

        // Merge other fields
        this.textureKeys = new ArrayList<>(this.textureKeys);
        this.textureKeys.addAll(material.textureKeys);

        // Merge incorrectForTool if present
        material.incorrectForTool.ifPresent(tagKey -> this.incorrectForTool = Optional.of(tagKey));

        // Merge color if present
        if (material.color.isPresent()) {
            this.color = material.color;
        }

        // Merge items
        this.items = new ArrayList<>(this.items);
        this.items.addAll(material.items);

        // Merge generateConverters if present
        if (material.generateConverters.isPresent()) {
            this.generateConverters = material.generateConverters;
        }

        // Merge string and double maps
        this.stringData.putAll(material.stringData);
        this.doubleMap.putAll(material.doubleMap);

        // Merge translation if present
        material.translation.ifPresent(value -> this.translation = Optional.of(value));

        // Merge icon and palette for client
        if (smartin.miapi.Environment.isClient()) {
            if (material.icon != null) {
                this.icon = material.icon;
            }
            if (material.palette != null) {
                this.palette = material.palette;
            }
        }
    }

    private static void mergeProperties(Map<String, Map<ModuleProperty<?>, Object>> source,
                                        Map<String, Map<ModuleProperty<?>, Object>> target) {
        source.forEach((key, sourceProperties) -> {
            Map<ModuleProperty<?>, Object> targetProperties = target.getOrDefault(key, new HashMap<>());

            sourceProperties.forEach((property, sourceValue) -> {
                if (targetProperties.containsKey(property)) {
                    Object targetValue = targetProperties.get(property);
                    targetProperties.put(property, ItemModule.merge(
                            property,
                            targetValue,
                            sourceValue,
                            MergeType.SMART
                    ));
                } else {
                    targetProperties.put(property, sourceValue);
                }
            });

            target.put(key, targetProperties);
        });
    }

    public void setID(ResourceLocation id) {
        this.id = id;
        List<String> g = new ArrayList<>(this.groups);
        g.addFirst(getStringID());
        groups = g;
        List<String> uiGroups = new ArrayList<>(this.guiGroups);
        uiGroups.addFirst(getStringID());
        guiGroups = uiGroups;
    }

    public void setData(Map<String, String> stringData, Map<String, Double> doubleMap) {
        this.stringData = stringData;
        this.doubleMap = doubleMap;
    }

    @Override
    public double getDouble(String property) {
        return doubleMap.getOrDefault(property, 0.0);
    }

    @Override
    public String getData(String property) {
        return stringData.getOrDefault(property, "");
    }

    @Environment(EnvType.CLIENT)
    public int renderIcon(GuiGraphics drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

    @Override
    public void addSmithingGroup() {
        if (!groups.contains("smithing")) {
            groups = new ArrayList<>(groups);
            groups.add("smithing");
        }
        if (!guiGroups.contains("smithing")) {
            guiGroups = new ArrayList<>(guiGroups);
            guiGroups.add("smithing");
        }
    }

    @Environment(EnvType.CLIENT)
    public boolean hasIcon() {
        return icon != null;
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @Override
    public List<String> getGroups() {
        return groups;
    }

    @Override
    public List<String> getGuiGroups() {
        return guiGroups;
    }

    @Override
    public Map<ModuleProperty<?>, Object> materialProperties(String key) {
        return propertyMap.getOrDefault(key, new HashMap<>());
    }

    @Override
    public Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return displayPropertyMap.getOrDefault(key, new HashMap<>());
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return new ArrayList<>(propertyMap.keySet());
    }

    @Override
    public List<String> getAllDisplayPropertyKeys() {
        return new ArrayList<>(displayPropertyMap.keySet());
    }

    @Override
    public List<String> getTextureKeys() {
        return textureKeys;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor() {
        return color.orElse(Color.BLACK.getRGB());
    }

    @Environment(EnvType.CLIENT)
    public FallbackColorer fallbackColorer = new FallbackColorer(this);

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController() {
        return palette == null ? fallbackColorer : palette;
    }

    @Override
    public double getValueOfItem(ItemStack item) {
        if (items == null) return 0;
        for (IngredientWithCount value : items) {
            if (value.ingredient.test(item)) {
                return value.count;
            }
        }
        return 0;
    }

    @Override
    public Double getPriorityOfIngredientItem(ItemStack item) {
        if (items == null) return null;
        for (IngredientWithCount value : items) {
            if (value.ingredient.test(item)) {
                return value.count;
            }
        }
        return null;
    }

    @Override
    public JsonObject getDebugJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().getAsJsonObject();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectForTool.orElse(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    }
}
