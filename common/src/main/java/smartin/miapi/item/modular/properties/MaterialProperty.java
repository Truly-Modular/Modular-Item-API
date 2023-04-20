package smartin.miapi.item.modular.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.StatResolver;

import javax.annotation.Nullable;
import java.util.*;

public class MaterialProperty implements ModuleProperty {
    public static final String key = "material";
    public static ModuleProperty materialProperty;
    public static Map<String, Material> materials = new HashMap<>();

    public MaterialProperty() {
        materialProperty = this;
        StatResolver.registerResolver(key, new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getKeyedProperties().get(key);
                try {
                    if (jsonData != null) {
                        jsonData = materials.get(jsonData.getAsString()).rawJson;
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsDouble();
                            }
                        }
                    }
                } catch (Exception suppressed) {
                    Miapi.LOGGER.error(suppressed.toString());
                    suppressed.printStackTrace();
                }
                return 0;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getProperties().get(materialProperty);
                try {
                    if (jsonData != null) {
                        jsonData = materials.get(jsonData.getAsString()).rawJson;
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsString();
                            }
                        }
                    }
                } catch (Exception suppressed) {

                }
                return "";
            }
        });
        ReloadEvents.MAIN.subscribe((isClient -> {
            materials.clear();
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.contains(key)) {
                    //load Material Logic
                    JsonParser parser = new JsonParser();
                    JsonObject obj = parser.parse(data).getAsJsonObject();
                    String key = obj.get("key").getAsString();
                    Material material = new Material();
                    material.key = key;
                    material.rawJson = obj;
                    materials.put(key, material);
                }
            });
        }), -1.0f);
    }

    public static List<String> getTextureKeys() {
        Set<String> textureKeys = new HashSet<>();
        textureKeys.add("base");
        for (Material material : materials.values()) {
            JsonArray textures = material.rawJson.getAsJsonObject().getAsJsonArray("textures");
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

    @Nullable
    public static Material getMaterial(ItemStack item) {
        for (Material material : materials.values()) {
            JsonObject obj = material.rawJson.getAsJsonObject();
            JsonArray items = obj.getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (Registry.ITEM.getId(item.getItem()).toString().equals(itemId)) {
                        return material;
                    }
                } else if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(tagId));
                    if (tag != null && item.isIn(tag)) {
                        return material;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static Material getMaterial(ItemModule.ModuleInstance instance) {
        JsonElement element = instance.getProperties().get(materialProperty);
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

    public class test extends InteractAbleWidget {
        private final int startX;
        private final int startY;
        /**
         * This is a Widget build to support Children and parse the events down to them.
         * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
         * unlike the base vanilla classes.
         * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
         * super method or handle the children yourself
         *
         * @param x      the X Position
         * @param y      the y Position
         * @param width  the width
         * @param height the height
         */
        public test(int x, int y, int width, int height) {
            super(x, y, width, height, Text.literal("Test"));
            startX = x+5;
            startY = y+5;
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            drawSquareBorder(matrices, x, y, width, height, 4, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            drawSquareBorder(matrices, x+5, y+5, 9, 9, 4, ColorHelper.Argb.getArgb(255, 0, 0, 0));
            drawSquareBorder(matrices, startX, startY, 9, 9, 4, ColorHelper.Argb.getArgb(255, 255, 255, 0));
        }
    }

    public class Material {
        public String key;
        public JsonElement rawJson;

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

        public List<String> getTextureKeys() {
            Set<String> textureKeys = new HashSet<>();
            JsonArray textures = rawJson.getAsJsonObject().getAsJsonArray("textures");
            for (JsonElement texture : textures) {
                textureKeys.add(texture.getAsString());
            }
            textureKeys.add("default");
            return new ArrayList<>(textureKeys);
        }

        public int getColor() {
            long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
            return (int) (longValue & 0xffffffffL);
        }

        public double getValueOfItem(ItemStack item) {
            JsonArray items = rawJson.getAsJsonObject().getAsJsonArray("items");

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (Registry.ITEM.getId(item.getItem()).toString().equals(itemId)) {
                        try {
                            return itemObj.get("value").getAsDouble();
                        } catch (Exception surpressed) {
                            return 1;
                        }
                    }
                } else if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registry.ITEM_KEY, new Identifier(tagId));
                    if (tag != null && item.isIn(tag)) {
                        try {
                            return itemObj.get("value").getAsDouble();
                        } catch (Exception surpressed) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
    }
}
