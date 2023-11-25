package smartin.miapi.modules.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.material.palette.EmptyMaterialPalette;
import smartin.miapi.modules.material.palette.MaterialPalette;
import smartin.miapi.modules.material.palette.PaletteCreators;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMaterial implements Material {
    public String key;
    protected JsonElement rawJson;
    @Nullable
    public MaterialIcons.MaterialIcon icon;
    protected MaterialPalette palette;

    public JsonMaterial(JsonObject element, boolean isClient) {
        rawJson = element;
        key = element.get("key").getAsString();

        if (isClient) {
            if (element.has("icon")) {
                JsonElement emnt = element.get("icon");
                if (emnt instanceof JsonPrimitive primitive && primitive.isString())
                    icon = new MaterialIcons.TextureMaterialIcon(new Identifier(primitive.getAsString()));
                else icon = MaterialIcons.getMaterialIcon(key, emnt);
            }

            if (element.has("color_palette")) {
                palette = PaletteCreators.paletteCreator.dispatcher().createPalette(element.get("color_palette"), this);
            } else {
                palette = new EmptyMaterialPalette(this);
            }
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        groups.add(key);
        if (rawJson.getAsJsonObject().has("groups")) {
            JsonArray groupsJson = rawJson.getAsJsonObject().getAsJsonArray("groups");
            for (JsonElement groupElement : groupsJson) {
                String group = groupElement.getAsString();
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        Map<ModuleProperty, JsonElement> propertyMap = new HashMap<>();
        JsonElement propertyElement = rawJson.getAsJsonObject().get("properties");
        if (propertyElement != null) {
            JsonElement element = propertyElement.getAsJsonObject().get(key);
            if (element != null) {
                element.getAsJsonObject().entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                    if (property != null) {
                        propertyMap.put(property, stringJsonElementEntry.getValue());
                    }
                });
            }
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
        for (String k : keys) {
            jsonData = jsonData.getAsJsonObject().get(k);
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
        if (rawJson.getAsJsonObject().has("textures")) {
            JsonArray textures = rawJson.getAsJsonObject().getAsJsonArray("textures");
            for (JsonElement texture : textures) {
                textureKeys.add(texture.getAsString());
            }
        }
        textureKeys.add("default");
        return new ArrayList<>(textureKeys);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor() {
        if (rawJson.getAsJsonObject().get("color") != null) {
            long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
            return (int) (longValue & 0xffffffffL);
        }
        return getPalette().getPaletteAverageColor().argb();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialPalette getPalette() {
        return palette;
    }

    @Environment(EnvType.CLIENT)
    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

    @Environment(EnvType.CLIENT)
    public boolean hasIcon() {
        return icon != null;
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

    @Override
    public Double getPriorityOfItem(ItemStack itemStack) {
        if (this.getRawElement("items") != null && this.getRawElement("items").isJsonArray()) {
            JsonArray items = this.getRawElement("items").getAsJsonArray();

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (Registries.ITEM.getId(itemStack.getItem()).toString().equals(itemId)) {
                        return 0.0;
                    }
                }
            }

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
                    if (tag != null && itemStack.isIn(tag)) {
                        return 1.0;
                    }
                }
            }
        }
        return null;
    }
}
