package smartin.miapi.material.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.blueprint.IngredientWithCount;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.material.MaterialIcons;
import smartin.miapi.material.base.IngredientController;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.base.PropertyController;
import smartin.miapi.material.palette.FallbackColorer;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleDataPropertiesManager;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.awt.*;
import java.util.List;
import java.util.*;

public class CodecMaterial implements Material {
    ResourceLocation id = Miapi.id("none");
    public Optional<JsonElement> iconJson;
    Optional<JsonElement> paletteJson;
    Optional<JsonElement> dyePaletteJson;
    Map<String, Map<ModuleProperty<?>, Object>> mergedAllAppliedProperties = new HashMap<>();
    Map<String, Map<ModuleProperty<?>, Object>> mergedAllVisualProperties = new HashMap<>();

    Map<String, Map<ModuleProperty<?>, Object>> normalProperties = new HashMap<>();
    Map<String, Map<ModuleProperty<?>, Object>> displayProperties = new HashMap<>();
    Map<String, Map<ModuleProperty<?>, Object>> hiddenProperties = new HashMap<>();
    public List<String> groups;
    List<String> guiGroups;
    public List<String> textureKeys;
    Optional<TagKey<Block>> incorrectForTool = Optional.empty();
    Optional<Integer> color = Optional.empty();
    public List<IngredientWithCount> items;
    public Map<String, String> stringData = new HashMap<>();
    public Map<String, Double> doubleMap = new HashMap<>();
    public Optional<Component> translation = Optional.empty();
    @Environment(EnvType.CLIENT)
    public MaterialIcons.MaterialIcon icon;
    @Environment(EnvType.CLIENT)
    protected MaterialRenderController palette;
    @Environment(EnvType.CLIENT)
    @Nullable
    protected MaterialRenderController dyeAblePalette;
    public Either<Boolean, List<Holder<Item>>> toGenerate = Either.left(false);
    public List<MaterialVariant> variants = new ArrayList<>();

    public static final Codec<CodecMaterial> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CodecMaterial, T>> decode(DynamicOps<T> ops, T input) {
            Map<String, String> stringData = new HashMap<>();
            Map<String, Double> doubleMap = new HashMap<>();

            try {
                ops.convertTo(JsonOpsBooleanPatched.INSTANCE, input)
                        .getAsJsonObject()
                        .asMap()
                        .forEach((key, element) -> {
                            if (element.isJsonPrimitive()) {
                                try {
                                    stringData.put(key, element.getAsString());
                                } catch (Exception e) {
                                    Miapi.LOGGER.debug("Failed to read '{}' as string: {}", key, e.getMessage());
                                }
                                try {
                                    doubleMap.put(key, element.getAsDouble());
                                } catch (Exception e) {
                                    Miapi.LOGGER.debug("Failed to read '{}' as double: {}", key, e.getMessage());
                                }
                            }
                        });
            } catch (Exception e) {
                Miapi.LOGGER.error("Error converting input during decode: {}", input, e);
                return DataResult.error(() -> "Failed to parse input: " + e.getMessage());
            }

            var dataResult = INNER_CODEC.decode(ops, input);

            if (dataResult.isSuccess()) {
                try {
                    dataResult.getOrThrow().getFirst().setData(stringData, doubleMap);
                } catch (Exception e) {
                    Miapi.LOGGER.error("Failed to attach parsed data to CodecMaterial", e);
                    return DataResult.error(() -> "Failed to attach parsed data: " + e.getMessage());
                }
            } else {
                Miapi.LOGGER.warn("INNER_CODEC failed to decode input: {}", input);
                Miapi.LOGGER.warn(dataResult.error().get().message());
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
            StatResolver.Codecs.JSONELEMENT_CODEC.optionalFieldOf("dye_color_palette").forGetter(material -> material.dyePaletteJson),
            Codec.STRING.listOf().optionalFieldOf("groups", new ArrayList<>()).forGetter(m ->
                    m.getGroups().stream().filter(g -> m.getGuiGroups().contains(g)).toList()),
            Codec.STRING.listOf().optionalFieldOf("hidden_groups", new ArrayList<>()).forGetter(m ->
                    m.getGroups().stream().filter(g -> !m.getGuiGroups().contains(g)).toList()),
            Codec.STRING.listOf().optionalFieldOf("gui_groups", new ArrayList<>()).forGetter(m ->
                    m.getGuiGroups().stream().filter(g -> !m.getGroups().contains(g)).toList()),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("properties", new HashMap<>()).forGetter(m -> PropertyController.toJsonMap(m.normalProperties)),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("display_properties", new HashMap<>()).forGetter(m -> PropertyController.toJsonMap(m.displayProperties)),
            Codec.unboundedMap(Codec.STRING, StatResolver.Codecs.JSONELEMENT_CODEC)
                    .optionalFieldOf("hidden_properties", new HashMap<>()).forGetter(m -> PropertyController.toJsonMap(m.hiddenProperties)),
            Codec.STRING.listOf().optionalFieldOf("textures", List.of("default")).forGetter(CodecMaterial::getTextureKeys),
            ResourceLocation.CODEC.optionalFieldOf("mining_level").forGetter(material -> Optional.of(material.getIncorrectBlocksForDrops().location())),
            ComponentSerialization.CODEC.optionalFieldOf("translation").forGetter(material -> material.translation),
            Codec.STRING.optionalFieldOf("color")
                    .forGetter(m -> Optional.of(Long.toHexString(((long) m.getColor(new ModuleInstance(ItemModule.empty.id(), Miapi.registryAccess))) & 0xFFFFFFFF))),
            IngredientWithCount.CODEC.listOf().optionalFieldOf("items", new ArrayList<>()).forGetter(material -> material.items),
            Codec.either(
                    Miapi.FIXED_BOOL_CODEC,
                    ItemStack.ITEM_NON_AIR_CODEC.listOf()
            ).optionalFieldOf("generate_converters", Either.left(false)).forGetter(m -> m.toGenerate),
            MaterialVariant.CODEC.listOf().optionalFieldOf("variants", new ArrayList<>()).forGetter(m -> m.variants)
    ).apply(instance, CodecMaterial::new));

    public CodecMaterial(Optional<JsonElement> iconJson,
                         Optional<JsonElement> paletteJson,
                         Optional<JsonElement> dyePaletteJson,
                         List<String> groups,
                         List<String> hiddenGroups,
                         List<String> guiGroups,
                         Map<String, JsonElement> property,
                         Map<String, JsonElement> visualProperty,
                         Map<String, JsonElement> hiddenProperty,
                         List<String> textureKeys,
                         Optional<ResourceLocation> incorrectForToolId,
                         Optional<Component> translation,
                         Optional<String> color,
                         List<IngredientWithCount> items,
                         Either<Boolean, List<Holder<Item>>> generateConverters,
                         List<MaterialVariant> variants) {
        this.variants = variants;
        this.iconJson = iconJson;
        this.paletteJson = paletteJson;
        this.dyePaletteJson = dyePaletteJson;
        this.groups = new ArrayList<>(groups);
        this.guiGroups = new ArrayList<>(guiGroups);
        this.guiGroups.addAll(groups);
        this.groups.addAll(hiddenGroups);
        this.textureKeys = textureKeys;
        this.translation = translation;
        if (incorrectForToolId.isPresent()) {
            var found = BuiltInRegistries.BLOCK.getTags().filter(pair -> pair.getFirst().location().equals(incorrectForToolId.get())).findAny();
            found.ifPresent(tagKeyNamedPair -> incorrectForTool = Optional.of(tagKeyNamedPair.getFirst()));
        }
        if (color.isPresent()) {
            try {
                long longValue = Long.parseLong(color.get(), 16);
                this.color = Optional.of((int) (longValue & 0xffffffffL));
            } catch (RuntimeException e) {
                Miapi.LOGGER.info("failed color decoding");
            }
        }
        this.items = items;
        this.toGenerate = generateConverters;
        hiddenProperty.forEach((type, json) -> {
            var data = ModuleDataPropertiesManager.resolvePropertiesFromJson(json);
            mergedAllAppliedProperties.put(type, data);
            this.hiddenProperties.put(type, data);
        });
        property.forEach((type, json) -> {
            var data = ModuleDataPropertiesManager.resolvePropertiesFromJson(json);
            mergedAllAppliedProperties.put(type, data);
            mergedAllVisualProperties.put(type, data);
            this.normalProperties.put(type, data);
        });
        visualProperty.forEach((type, json) -> {
            var data = ModuleDataPropertiesManager.resolvePropertiesFromJson(json);
            mergedAllVisualProperties.put(type, data);
            displayProperties.put(type, data);
        });
        if (smartin.miapi.Environment.isClient()) {
            clientSetup(iconJson, paletteJson, dyePaletteJson);
        }
    }

    public void setup() {
        if (smartin.miapi.Environment.isClient()) {
            clientSetup(iconJson, paletteJson, dyePaletteJson);
        }
    }

    @Environment(EnvType.CLIENT)
    private void clientSetup(Optional<JsonElement> iconJson, Optional<JsonElement> paletteJson, Optional<JsonElement> dyePaletteJson) {
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
        dyePaletteJson.ifPresent(element -> dyeAblePalette =
                MaterialRenderControllers.creators.get(
                                element
                                        .getAsJsonObject()
                                        .get("type")
                                        .getAsString())
                        .createPalette(element, this));
    }

    public CodecMaterial copy() {
        CodecMaterial copy = new CodecMaterial(
                this.iconJson,
                this.paletteJson,
                this.dyePaletteJson,
                new ArrayList<>(this.groups),
                List.of(),
                new ArrayList<>(this.guiGroups),
                PropertyController.toJsonMap(this.normalProperties),
                PropertyController.toJsonMap(this.hiddenProperties),
                PropertyController.toJsonMap(this.displayProperties),
                new ArrayList<>(this.textureKeys),
                this.incorrectForTool.map(TagKey::location),
                this.translation,
                this.color.map(Integer::toHexString),
                new ArrayList<>(this.items),
                this.toGenerate,
                new ArrayList<>(this.variants)
        );

        // Copy non-constructor fields
        if(this.id==null){
            throw new RuntimeException("Cannot create copy for a material without an ID");
        }
        copy.id = this.id;
        copy.stringData = new HashMap<>(this.stringData);
        copy.doubleMap = new HashMap<>(this.doubleMap);
        copy.mergedAllAppliedProperties = new HashMap<>(this.mergedAllAppliedProperties);
        copy.mergedAllVisualProperties = new HashMap<>(this.mergedAllVisualProperties);
        copy.incorrectForTool = this.incorrectForTool;
        copy.translation = this.translation;

        copy.iconJson = this.iconJson;
        copy.paletteJson = this.paletteJson;
        copy.dyePaletteJson = this.dyePaletteJson;
        if (smartin.miapi.Environment.isClient()) {
            copyClient(copy);
        }

        return copy;
    }

    @Environment(EnvType.CLIENT)
    private void copyClient(CodecMaterial copy) {
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
        if (copy.dyePaletteJson.isPresent()) {
            copy.dyeAblePalette = MaterialRenderControllers.creators.get(copy.dyePaletteJson.get().getAsJsonObject().get("type").getAsString()).createPalette(copy.dyePaletteJson.get(), this);
            if (copy.color.isEmpty()) {
                copy.color = Optional.of(dyeAblePalette.getAverageColor().argb());
            }
        }
    }

    public void merge(CodecMaterial material) {
        if (material.iconJson.isPresent()) {
            this.iconJson = material.iconJson;
        }
        if (material.paletteJson.isPresent()) {
            this.paletteJson = material.paletteJson;
            if(Platform.getEnv() == EnvType.CLIENT){
                this.palette = MaterialRenderControllers.creators.get(this.paletteJson.get().getAsJsonObject().get("type").getAsString()).createPalette(this.paletteJson.get(), this);
            }
        }

        if (material.dyePaletteJson.isPresent()) {
            this.dyePaletteJson = material.paletteJson;
            if(Platform.getEnv() == EnvType.CLIENT) {
                this.dyeAblePalette = MaterialRenderControllers.creators.get(this.dyePaletteJson.get().getAsJsonObject().get("type").getAsString()).createPalette(this.dyePaletteJson.get(), this);
            }
        }
        // Merge groups and guiGroups
        this.groups = new ArrayList<>(this.groups);
        this.groups.addAll(material.groups);
        this.groups = groups.stream().distinct().toList();
        this.guiGroups = new ArrayList<>(this.guiGroups);
        this.guiGroups.addAll(material.guiGroups);
        this.guiGroups = new ArrayList<>(guiGroups.stream().distinct().toList());
        ResourceLocation toRemove = material.getID();
        if (toRemove != null) {
            this.guiGroups.remove(toRemove.toLanguageKey().replace("/", "."));
        }
        toRemove = this.getID();
        if (toRemove != null) {
            this.guiGroups.remove(toRemove.toLanguageKey().replace("/", "."));
        }

        // Merge properties
        mergeProperties(material.mergedAllAppliedProperties, this.mergedAllAppliedProperties);
        mergeProperties(material.mergedAllVisualProperties, this.mergedAllVisualProperties);

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
        this.toGenerate = material.toGenerate;

        // Merge string and double maps
        this.stringData.putAll(material.stringData);
        this.doubleMap.putAll(material.doubleMap);

        // Merge translation if present
        material.translation.ifPresent(value -> this.translation = Optional.of(value));

        // Merge icon and palette for client
        if (smartin.miapi.Environment.isClient()) {
            mergeClient(material);
        }
    }

    @Environment(EnvType.CLIENT)
    private void mergeClient(CodecMaterial material) {
        if (material.icon != null) {
            this.icon = material.icon;
        }
        if (material.palette != null) {
            this.palette = material.palette;
        }
        this.dyeAblePalette = material.dyeAblePalette;
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
        if (id == null) {
            throw new RuntimeException("trying to set a material id to NULL.");
        }
        this.id = id;
        List<String> g = new ArrayList<>(this.groups);
        g.addFirst(getStringID());
        groups = g;
        guiGroups = new ArrayList<>(this.guiGroups);
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
        return mergedAllAppliedProperties.getOrDefault(key, new HashMap<>());
    }

    @Override
    public Map<ModuleProperty<?>, Object> getDisplayMaterialProperties(String key) {
        return mergedAllVisualProperties.getOrDefault(key, new HashMap<>());
    }

    @Override
    public List<String> getAllPropertyKeys() {
        return new ArrayList<>(mergedAllAppliedProperties.keySet());
    }

    @Override
    public List<String> getAllDisplayPropertyKeys() {
        return new ArrayList<>(mergedAllVisualProperties.keySet());
    }

    @Override
    public List<String> getTextureKeys() {
        return textureKeys;
    }

    private static List<TieredItem> tieredItems;
    private static List<ArmorItem> armorItems;

    private static List<TieredItem> getTieredItems() {
        if (tieredItems == null) {
            tieredItems = BuiltInRegistries.ITEM.stream()
                    .filter(TieredItem.class::isInstance)
                    .map(i -> (TieredItem) i)
                    .toList();
        }
        return tieredItems;
    }

    private static List<ArmorItem> getArmorItems() {
        if (armorItems == null) {
            armorItems = BuiltInRegistries.ITEM.stream()
                    .filter(ArmorItem.class::isInstance)
                    .map(i -> (ArmorItem) i)
                    .toList();
        }
        return armorItems;
    }

    public void generateConverters(boolean isClient) {
        if (toGenerate.left().isPresent()) {
            if (toGenerate.left().get()) {
                List<TieredItem> matchedTiered = null;
                List<ArmorItem> matchedArmor = null;

                for (TieredItem item : getTieredItems()) {
                    if (this.getPriorityOfIngredientItem(item.getDefaultInstance()) != null) {
                        if (matchedTiered == null) matchedTiered = new ArrayList<>();
                        matchedTiered.add(item);
                    }
                }

                for (ArmorItem item : getArmorItems()) {
                    if (this.getPriorityOfIngredientItem(item.getDefaultInstance()) != null) {
                        if (matchedArmor == null) matchedArmor = new ArrayList<>();
                        matchedArmor.add(item);
                    }
                }

                if ((matchedTiered != null && !matchedTiered.isEmpty()) ||
                    (matchedArmor != null && !matchedArmor.isEmpty())) {

                    MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(
                            this,
                            matchedTiered != null ? matchedTiered : List.of(),
                            matchedArmor != null ? matchedArmor : List.of(),
                            isClient
                    );
                }
            }
        } else {
            List<Item> items = toGenerate.right().get().stream().map(Holder::value).toList();
            MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(this,
                    items.stream().filter(TieredItem.class::isInstance).map(i -> (TieredItem) i).toList(),
                    items.stream().filter(ArmorItem.class::isInstance).map(i -> (ArmorItem) i).toList(),
                    isClient);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor(ModuleInstance context) {
        return color.orElse(Color.BLACK.getRGB());
    }

    @Environment(EnvType.CLIENT)
    public FallbackColorer fallbackColorer;

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode) {
        if (context.cache().getStack() != null && dyeAblePalette != null &&
            ColorProperty.hasColor(context.cache().getStack(), context)) {
            return dyeAblePalette;
        }
        if (palette == null) {
            if (fallbackColorer == null) {
                fallbackColorer = new FallbackColorer(this);
            }
            return fallbackColorer;
        }
        return palette;
    }

    @Override
    public boolean canBeDyed() {
        return dyePaletteJson.isPresent();
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

    public Ingredient getRepairIngredient() {
        return IngredientController.mergeIngredients(items.stream().map(ingredientWithCount -> ingredientWithCount.ingredient));
    }

    @Override
    public JsonObject getDebugJson() {
        return CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, this).getOrThrow().getAsJsonObject();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectForTool.orElse(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
    }

    public Material getMaterial(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties) {
        ConditionManager.ConditionContext context = ConditionManager.moduleContext(moduleInstance, properties);
        for (MaterialVariant variant : variants) {
            if (variant.condition().isAllowed(context)) {
                CodecMaterial material = this.copy();
                material.setID(this.getID());
                material.merge(variant.overwrite());
                material.setID(this.getID());
                material.setup();
                return material;
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        ResourceLocation id = getID();
        // Null-safety: if ID is null (shouldn't happen), use a fallback
        if (id == null) {
            return 0;
        }
        return id.hashCode() + 13 * variants.size();
    }

    public Component getTranslation() {
        return translation.orElseGet(() -> Component.translatable("miapi.material." + getStringID()));
    }
}
