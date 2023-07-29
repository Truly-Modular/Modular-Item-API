package smartin.miapi.modules.properties;

import com.google.gson.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.InterfaceDispatcher;
import com.redpxnda.nucleus.datapack.codec.MiscCodecs;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
        public Identifier materialColorPalette = Material.baseColorPalette;

        public JsonMaterial(JsonObject element) {
            rawJson = element;
            key = element.get("key").getAsString();
            if (element.has("color_palette") && Platform.getEnvironment().equals(Env.CLIENT)) {
                setupMaterialPalette(element.get("color_palette"));
            }
        }

        public void setupMaterialPalette(JsonElement json) {
            materialColorPalette = PaletteCreators.paletteCreator.dispatcher().createPalette(json, key);
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
            int id = 10;
            RenderSystem.setShaderTexture(id, materialColorPalette);
            RenderSystem.bindTexture(id);
            int j = RenderSystem.getShaderTexture(id);
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
        public static final Map<String, PaletteCreator> creators = new HashMap<>();
        public static final Map<String, FillerFunction> fillers = new HashMap<>();
        public static final InterfaceDispatcher<PaletteCreator> paletteCreator = InterfaceDispatcher.of(creators, "type");

        public static FillerFunction interpolateFiller;

        static {
            if (Platform.getEnvironment().equals(Env.CLIENT)) {
                interpolateFiller = (last, current, next, lX, cX, nX, placer) -> {
                    for (int i = lX; i < cX; i++) {
                        float delta = (i-lX) / (float) (cX-lX);
                        placer.place(
                                new Color(
                                        MathHelper.lerp(delta, last.r, current.r),
                                        MathHelper.lerp(delta, last.g, current.g),
                                        MathHelper.lerp(delta, last.b, current.b),
                                        MathHelper.lerp(delta, last.a, current.a)
                                ), i, 0
                        );
                    }
                };
                fillers.put("interpolate", interpolateFiller);
                fillers.put("current_to_last", (last, current, next, lX, cX, nX, placer) -> {
                    for (int i = lX; i < cX; i++) {
                        placer.place(current, i, 0);
                    }
                });
                fillers.put("last_to_current", (last, current, next, lX, cX, nX, placer) -> {
                    for (int i = lX; i < cX; i++) {
                        placer.place(last, i, 0);
                    }
                });
                fillers.put("current_last_shared", (last, current, next, lX, cX, nX, placer) -> {
                    for (int i = lX; i < cX; i++) {
                        float delta = (i-lX) / (float) (cX-lX);
                        Color color = delta < 0.5 ? last : current;
                        placer.place(color, i, 0);
                    }
                });

                creators.put("texture", (json, material) -> {
                    if (json instanceof JsonObject object && object.has("location"))
                        return new Identifier(object.get("location").getAsString());
                    else if (json instanceof JsonObject)
                        throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Missing member 'location'.");
                    else
                        throw new JsonParseException("ModularItem API failed to parse texture sampling palette for material '" + material + "'! Not a JSON object -> " + json);
                });

                Codec<Integer> stringToIntCodec = Codec.STRING.xmap(Integer::parseInt, String::valueOf);
                creators.put("grayscale_map", (json, material) -> {
                    if (json instanceof JsonObject object) {
                        if (!object.has("colors"))
                            throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Missing member 'colors'.");

                        JsonElement element = object.get("colors");
                        Map<Integer, Color> colors = new HashMap<>(MiscCodecs.quickParse(
                                element, Codec.unboundedMap(stringToIntCodec, MiscCodecs.COLOR),
                                s -> Miapi.LOGGER.error("Failed to create material palette color map from JSON '" + element + "'! -> " + s)
                        ));
                        String key = object.has("filler") ? object.get("filler").getAsString() : "interpolate";
                        FillerFunction filler = fillers.getOrDefault(key, interpolateFiller);

                        Color black = new Color(0, 0, 0, 255);
                        Color white = new Color(255, 255, 255, 255);
                        if (!colors.containsKey(0))
                            colors.put(0, black);
                        if (!colors.containsKey(255))
                            colors.put(255, white);

                        Identifier identifier = new Identifier(Miapi.MOD_ID, "textures/generated_materials/" + material);
                        NativeImage image = new NativeImage(256, 1, false);
                        PixelPlacer placer = (color, x, y) -> image.setColor(x, y, color.abgr());

                        List<Map.Entry<Integer, Color>> list = colors.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
                        for (int i = 0; i < list.size(); i++) {
                            Map.Entry<Integer, Color> last = i == 0 ? Map.entry(0, black) : list.get(i-1);
                            Map.Entry<Integer, Color> current = list.get(i);
                            Map.Entry<Integer, Color> next = i == list.size()-1 ? Map.entry(255, white) : list.get(i+1);

                            filler.fill(
                                    last.getValue(),
                                    current.getValue(),
                                    next.getValue(),
                                    last.getKey(),
                                    current.getKey(),
                                    next.getKey(),
                                    placer
                            );
                            image.setColor(current.getKey(), 0, current.getValue().abgr());
                        }
                        image.untrack();

                        /*Path path = Path.of("miapi_dev").resolve("material_" + material + "_palette.png");
                        try {
                            image.writeTo(path);
                        } catch (IOException e) {
                            //throw new RuntimeException(e);
                        }*/

                        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(image));
                        return identifier;
                    }
                    throw new JsonParseException("ModularItem API failed to parse grayscale_map sampling palette for material '" + material + "'! Not a JSON object -> " + json);
                });
            }
        }
    }
    public interface PaletteCreator {
        Identifier createPalette(JsonElement element, String materialKey);
    }
    public interface FillerFunction {
        void fill(Color last, Color current, Color next, int lastX, int currentX, int nextX, PixelPlacer placer);
    }
    public interface PixelPlacer {
        void place(Color color, int x, int y);
    }

    public interface Material {
        Identifier baseColorPalette = new Identifier(Miapi.MOD_ID, "textures/item/materials/base_palette.png");

        String getKey();

        static int getColor(String color) {
            if (color.equals("")) return ColorHelper.Argb.getArgb(255, 255, 255, 255);
            long longValue = Long.parseLong(color, 16);
            return (int) (longValue & 0xffffffffL);
        }

        List<String> getGroups();

        VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader);

        static VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader, Identifier texture) {
            int id = 10;
            RenderSystem.setShaderTexture(id, texture);
            RenderSystem.bindTexture(id);
            int j = RenderSystem.getShaderTexture(id);
            shader.addSampler("MatColors", j);
            return provider.getBuffer(layer);
        }

        Map<ModuleProperty, JsonElement> materialProperties(String key);

        JsonElement getRawElement(String key);

        double getDouble(String property);

        String getData(String property);

        List<String> getTextureKeys();

        int getColor();

        double getValueOfItem(ItemStack item);
    }
}
