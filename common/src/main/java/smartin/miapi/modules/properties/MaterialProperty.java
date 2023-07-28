package smartin.miapi.modules.properties;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.gson.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.redpxnda.nucleus.datapack.codec.MiscCodecs;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the Property relating to materials of a Module
 */
public class MaterialProperty implements ModuleProperty {
    public static final String KEY = "material";
    public static ModuleProperty property;
    public static Map<String, Material> materials = new HashMap<>();

    public MaterialProperty() {
        property = this;
        StatResolver.registerResolver(KEY, new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getKeyedProperties().get(KEY);
                try {
                    if (jsonData != null) {
                        String materialKey = jsonData.getAsString();
                        Material material = materials.get(materialKey);
                        if (material != null) {
                            return material.getDouble(data);
                        }
                    }
                } catch (Exception exception) {
                    Miapi.LOGGER.warn("Error during Material Resolve");
                    Miapi.LOGGER.error(exception.getMessage());
                    exception.printStackTrace();
                }
                return 0;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getProperties().get(property);
                try {
                    if (jsonData != null) {
                        String materialKey = jsonData.getAsString();
                        Material material = materials.get(materialKey);
                        if (material != null) {
                            return material.getData(data);
                        } else {
                            Miapi.LOGGER.warn("Material " + materialKey + " not found");
                        }
                    }
                } catch (Exception exception) {
                    Miapi.LOGGER.warn("Error during Material Resolve");
                    exception.printStackTrace();
                }
                return "";
            }
        });
        Miapi.registerReloadHandler(ReloadEvents.MAIN, "materials", materials, (isClient, path, data) -> {
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(data).getAsJsonObject();
            JsonMaterial material = new JsonMaterial(obj);
            materials.put(material.getKey(), material);
        }, -1f);
    }

    public static List<String> getTextureKeys() {
        Set<String> textureKeys = new HashSet<>();
        textureKeys.add("base");
        for (Material material : materials.values()) {
            textureKeys.add(material.getKey());
            JsonArray textures = material.getRawElement("textures").getAsJsonArray();
            for (JsonElement texture : textures) {
                textureKeys.add(texture.getAsString());
            }
        }
        return new ArrayList<>(textureKeys);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case EXTEND -> {
                return old;
            }
            case SMART, OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    @Nullable
    public static MaterialProperty.Material getMaterial(ItemStack item) {
        for (Material material : materials.values()) {
            JsonArray items = material.getRawElement("items").getAsJsonArray();

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (Registries.ITEM.getId(item.getItem()).toString().equals(itemId)) {
                        return material;
                    }
                }
            }

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
                    if (tag != null && item.isIn(tag)) {
                        return material;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static MaterialProperty.Material getMaterial(JsonElement element) {
        if (element != null) {
            return materials.get(element.getAsString());
        }
        return null;
    }

    @Nullable
    public static MaterialProperty.Material getMaterial(ItemModule.ModuleInstance instance) {
        JsonElement element = instance.getProperties().get(property);
        if (element != null) {
            return materials.get(element.getAsString());
        }
        return null;
    }

    public static void setMaterial(ItemModule.ModuleInstance instance, String material) {
        String propertyString = instance.moduleData.computeIfAbsent("properties", (key) -> {
            return "{material:empty}";
        });
        JsonObject moduleJson = Miapi.gson.fromJson(propertyString, JsonObject.class);
        moduleJson.addProperty("material", material);
        instance.moduleData.put("properties", Miapi.gson.toJson(moduleJson));
    }

    public static class JsonMaterial implements Material {
        public String key;
        protected JsonElement rawJson;
        public Identifier materialColorPalette = new Identifier(Miapi.MOD_ID, "textures/item/material_test.png");

        public JsonMaterial(JsonObject element) {
            rawJson = element;
            key = element.get("key").getAsString();
            if (element.has("color_palette") && Platform.getEnvironment().equals(Env.CLIENT)) {
                setupMaterialPalette(element.get("color_palette"));
            }
        }

        public void setupMaterialPalette(JsonElement json) {
            materialColorPalette = PaletteCreators.createPaletteIdentifier(key, json);
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public List<String> getGroups() {
            List<String> groups = new ArrayList<>();
            groups.add(key);
            JsonArray groupsJson = rawJson.getAsJsonObject().getAsJsonArray("groups");
            for (JsonElement groupElement : groupsJson) {
                String group = groupElement.getAsString();
                groups.add(group);
            }
            return groups;
        }

        @Override
        public VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader) {
            int txtId = 10;
            RenderSystem.setShaderTexture(txtId, materialColorPalette);
            RenderSystem.bindTexture(txtId);
            int j = RenderSystem.getShaderTexture(txtId);
            shader.addSampler("MatColors", j);
            return provider.getBuffer(layer);
        }

        @Override
        public Map<ModuleProperty, JsonElement> materialProperties(String key) {
            JsonElement element = rawJson.getAsJsonObject().get(key);
            Map<ModuleProperty, JsonElement> propertyMap = new HashMap<>();
            if (element != null) {
                element.getAsJsonObject().entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                    if (property != null) {
                        propertyMap.put(property, stringJsonElementEntry.getValue());
                    }
                });
            }
            return propertyMap;
        }

        @Override
        public JsonElement getRawElement(String key) {
            return rawJson.getAsJsonObject().get(key);
        }

        @Override
        public double getDouble(String property) {
            String[] keys = property.split("\\.");
            JsonElement jsonData = rawJson;
            for (String key : keys) {
                jsonData = jsonData.getAsJsonObject().get(key);
                if (jsonData == null) {
                    break;
                }
            }
            if (jsonData != null) {
                return jsonData.getAsDouble();
            }
            return 0;
        }

        @Override
        public String getData(String property) {
            String[] keys = property.split("\\.");
            JsonElement jsonData = rawJson;
            for (String key : keys) {
                jsonData = jsonData.getAsJsonObject().get(key);
                if (jsonData == null) {
                    break;
                }
            }
            if (jsonData != null) {
                return jsonData.getAsString();
            }
            return "";
        }

        @Override
        public List<String> getTextureKeys() {
            List<String> textureKeys = new ArrayList<>();
            JsonArray textures = rawJson.getAsJsonObject().getAsJsonArray("textures");
            for (JsonElement texture : textures) {
                textureKeys.add(texture.getAsString());
            }
            textureKeys.add("default");
            return new ArrayList<>(textureKeys);
        }

        @Override
        public int getColor() {
            long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
            return (int) (longValue & 0xffffffffL);
        }

        @Override
        public double getValueOfItem(ItemStack item) {
            JsonArray items = rawJson.getAsJsonObject().getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (Registries.ITEM.getId(item.getItem()).toString().equals(itemId)) {
                        try {
                            return itemObj.get("value").getAsDouble();
                        } catch (Exception surpressed) {
                            return 1;
                        }
                    }
                } else if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
                    if (tag != null && item.isIn(tag)) {
                        try {
                            return itemObj.get("value").getAsDouble();
                        } catch (Exception suppressed) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
    }
    public static final class PaletteCreators {
        public static void setup() {
            var classLoading = PaletteCreators.class;
        }
        public static final Map<String, BiFunction<String, JsonElement, Identifier>> creators = new HashMap<>();
        public static Identifier createPaletteIdentifier(String key, JsonElement element) {
            Map<String, Function<JsonElement, Identifier>> mappedCreators = creators.entrySet().stream()
                    .map(entry -> new Pair<String, Function<JsonElement, Identifier>>(entry.getKey(), jsonElement -> entry.getValue().apply(key, jsonElement)))
                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            Codec<Identifier> codec = MiscCodecs.functionMapCodec("type", mappedCreators).flatComapMap(
                    MiscCodecs.FunctionHolder::apply,
                    texture -> DataResult.error(() -> "blud, how the fuck do you expect me to turn an identifier into the function that created it? i aint doing all that")
            );
            return MiscCodecs.quickParse(element, codec, s -> Miapi.LOGGER.error("Failed to parse palette creator! -> " + s));
        }

        static {
            if (Platform.getEnvironment().equals(Env.CLIENT)) {
                creators.put("texture", (material, json) -> {
                    if (json instanceof JsonObject object && object.has("location"))
                        return new Identifier(object.get("location").getAsString());
                    else if (json instanceof JsonObject)
                        throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Missing member 'location'.");
                    else
                        throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Not a JSON object -> " + json);
                });

                Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);
                creators.put("grayscale_map", (material, json) -> {
                    if (json instanceof JsonObject object) {
                        if (!object.has("colors"))
                            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Missing member 'colors'.");

                        JsonElement element = object.get("colors");
                        Map<Integer, Integer> colors = MiscCodecs.quickParse(
                                element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                                s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
                        );

                        Identifier identifier = new Identifier(Miapi.MOD_ID, "textures/generated_materials/" + material);
                        NativeImage image = new NativeImage(256, 1, false);
                        PeekingIterator<Map.Entry<Integer, Integer>> iter = Iterators.peekingIterator(colors.entrySet().stream().sorted(Map.Entry.comparingByKey()).iterator());
                        while (iter.hasNext()) {
                            var entry = iter.next();
                            image.setColor(entry.getKey(), 0, entry.getValue());
                        }

                        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(image));
                        return identifier;
                    }
                    throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Not a JSON object -> " + json);
                });
            }
        }
    }

    public interface Material {

        String getKey();

        static int getColor(String color) {
            if (color.equals("")) return ColorHelper.Argb.getArgb(255, 255, 255, 255);
            long longValue = Long.parseLong(color, 16);
            return (int) (longValue & 0xffffffffL);
        }

        List<String> getGroups();

        VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader);

        Map<ModuleProperty, JsonElement> materialProperties(String key);

        JsonElement getRawElement(String key);

        double getDouble(String property);

        String getData(String property);

        List<String> getTextureKeys();

        int getColor();

        double getValueOfItem(ItemStack item);
    }
}
