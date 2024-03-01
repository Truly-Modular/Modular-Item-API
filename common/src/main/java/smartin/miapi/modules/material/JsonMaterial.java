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
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.material.palette.FallbackColorer;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.FakeTranslation;
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
    protected MaterialRenderController palette;
    public Map<String, Map<ModuleProperty, JsonElement>> propertyMap = new HashMap<>();

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
                palette = MaterialRenderControllers.paletteCreator.dispatcher().createPalette(element.get("color_palette"), this);
            } else {
                palette = new FallbackColorer(this);
            }
            if (element.has("fake_translation") && element.has("translation")) {
                FakeTranslation.translations.put(element.get("translation").getAsString(), element.get("fake_translation").getAsString());
            }
        }
        mergeJson(rawJson, isClient);
    }

    public void mergeJson(JsonElement rootElement, boolean isClient) {
        rootElement.getAsJsonObject().asMap().forEach((elementName, propertyElement) -> {
            switch (elementName) {
                case "properties": {
                    propertyElement.getAsJsonObject().asMap().forEach((id, element) -> {
                        if (element != null) {
                            element.getAsJsonObject().entrySet().forEach(stringJsonElementEntry -> {
                                ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                                Map<ModuleProperty, JsonElement> specificPropertyMap = propertyMap.getOrDefault(id, new HashMap<>());
                                if (property != null) {
                                    if (specificPropertyMap.containsKey(property)) {
                                        specificPropertyMap.put(property, property.merge(specificPropertyMap.get(property), stringJsonElementEntry.getValue(), MergeType.SMART));
                                    } else {
                                        specificPropertyMap.put(property, stringJsonElementEntry.getValue());
                                    }
                                }
                                propertyMap.put(id, specificPropertyMap);
                            });
                        }
                    });
                    break;
                }
                case "color_palette": {
                    if (isClient) {
                        palette = MaterialRenderControllers.paletteCreator.dispatcher().createPalette(propertyElement, this);
                    }
                    break;
                }
                case "icon": {
                    if (isClient) {
                        JsonElement emnt = propertyElement;
                        if (emnt instanceof JsonPrimitive primitive && primitive.isString())
                            icon = new MaterialIcons.TextureMaterialIcon(new Identifier(primitive.getAsString()));
                        else icon = MaterialIcons.getMaterialIcon(key, emnt);
                    }
                    break;
                }
                case "fake_translation": {
                    if (isClient) {
                        FakeTranslation.translations.put(rawJson.getAsJsonObject().get("translation").getAsString(), propertyElement.getAsString());
                    }
                    break;
                }
                default: {
                    rawJson.getAsJsonObject().add(elementName, propertyElement);
                }
            }
        });
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
        if (rawJson.getAsJsonObject().has("hidden_groups")) {
            JsonArray groupsJson = rawJson.getAsJsonObject().getAsJsonArray("hidden_groups");
            for (JsonElement groupElement : groupsJson) {
                String group = groupElement.getAsString();
                groups.add(group);
            }
        }
        return groups;
    }

    @Override
    public List<String> getGuiGroups() {
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
        return propertyMap.getOrDefault(key, new HashMap<>());
    }

    @Override
    public List<String> getAllPropertyKeys() {
        JsonElement propertyElement = rawJson.getAsJsonObject().get("properties");
        if (propertyElement != null) {
            return new ArrayList<>(propertyElement.getAsJsonObject().keySet());
        }
        return new ArrayList<>();
    }

    public JsonElement getRawElement(String key) {
        return rawJson.getAsJsonObject().get(key);
    }

    @Override
    public double getDouble(String property) {
        String[] keys = property.split("\\.");
        JsonElement jsonData = rawJson;
        for (String k : keys) {
            jsonData = jsonData.getAsJsonObject().get(k);
            if (jsonData == null || !jsonData.isJsonObject()) {
                break;
            }
        }
        if (jsonData != null && jsonData.isJsonNull()) {
            Miapi.LOGGER.info(String.valueOf(rawJson));
        }
        if (jsonData != null && jsonData.isJsonPrimitive()) {
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
            if (jsonData == null || !jsonData.isJsonObject()) {
                break;
            }
        }
        if (jsonData != null && jsonData.isJsonPrimitive()) {
            return jsonData.getAsString();
        }
        return "";
    }

    public boolean generateConverters() {
        if (rawJson.getAsJsonObject().has("generate_converters")) {
            JsonElement element = rawJson.getAsJsonObject().get("generate_converters");
            if (element != null && !element.isJsonNull() && element.isJsonPrimitive()) {
                return element.getAsBoolean();
            }
        }
        return false;
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
        return getPalette().getAverageColor().argb();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getPalette() {
        if (palette == null) {
            return new FallbackColorer(this);
        }
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
            }else if(itemObj.has("ingredient")){
                Ingredient ingredient = Ingredient.fromJson(itemObj.get("ingredient"));
                if(ingredient.test(item)){
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
    public Double getPriorityOfIngredientItem(ItemStack itemStack) {
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

                if (itemObj.has("ingredient")) {
                    Ingredient ingredient = Ingredient.fromJson(itemObj.get("ingredient"));
                    if (ingredient.test(itemStack)) {
                        return 5.0;
                    }
                }
            }

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
                    if (tag != null && itemStack.isIn(tag)) {
                        return 10.0;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public JsonObject getDebugJson() {
        return rawJson.getAsJsonObject();
    }
}
