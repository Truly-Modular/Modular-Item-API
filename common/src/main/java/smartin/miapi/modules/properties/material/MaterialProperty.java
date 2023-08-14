package smartin.miapi.modules.properties.material;

import com.google.gson.*;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

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
            if (materials.containsKey(material.getKey())) {
                Miapi.LOGGER.warn("Overwriting Materials isnt supported yet and may cause issues. Material from  " + path + " is overwriting " + material.getKey());
            }
            materials.put(material.getKey(), material);
        }, -1f);
        ReloadEvents.END.subscribe((isClient -> {
            Miapi.LOGGER.info("Loaded " + materials.size() + " Materials");
        }));
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
    public static Material getMaterial(ItemStack item) {
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
    public static Material getMaterial(JsonElement element) {
        if (element != null) {
            return materials.get(element.getAsString());
        }
        return null;
    }

    @Nullable
    public static Material getMaterial(ItemModule.ModuleInstance instance) {
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

    public interface PaletteCreator {
        Identifier createPalette(JsonElement element, String materialKey);
    }

    public interface FillerFunction {
        void fill(Color last, Color current, Color next, int lastX, int currentX, int nextX, PixelPlacer placer);
    }

    public interface PixelPlacer {
        void place(Color color, int x, int y);
    }

}
