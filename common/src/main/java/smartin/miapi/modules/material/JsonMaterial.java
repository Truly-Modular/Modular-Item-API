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
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.material.palette.FallbackColorer;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.FakeTranslation;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

public class JsonMaterial implements Material {
    public String key;
    protected JsonElement rawJson;
    @Nullable
    public MaterialIcons.MaterialIcon icon;
    protected MaterialRenderController palette;
    public Map<String, Map<ModuleProperty, JsonElement>> propertyMap = new HashMap<>();
    public Map<String, Map<ModuleProperty, JsonElement>> displayPropertyMap = new HashMap<>();
    public Optional<Boolean> generateConvertersOptional = Optional.empty();

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
                    mergeProperties(propertyElement, propertyMap);
                    mergeProperties(propertyElement, displayPropertyMap);
                    break;
                }
                case "display_properties": {
                    mergeProperties(propertyElement, displayPropertyMap);
                    break;
                }
                case "hidden_properties": {
                    mergeProperties(propertyElement, propertyMap);
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
                case "generate_converters": {
                    generateConvertersOptional = Optional.of(propertyElement.getAsBoolean());
                    break;
                }
                default: {
                    rawJson.getAsJsonObject().add(elementName, propertyElement);
                }
            }
        });
    }

    private static void mergeProperties(JsonElement propertyElement, Map<String, Map<ModuleProperty, JsonElement>> properties) {
        propertyElement.getAsJsonObject().asMap().forEach((id, element) -> {
            if (element != null) {
                element.getAsJsonObject().entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                    Map<ModuleProperty, JsonElement> specificPropertyMap = properties.getOrDefault(id, new HashMap<>());
                    if (property != null) {
                        if (specificPropertyMap.containsKey(property)) {
                            specificPropertyMap.put(property, property.merge(specificPropertyMap.get(property), stringJsonElementEntry.getValue(), MergeType.SMART));
                        } else {
                            specificPropertyMap.put(property, stringJsonElementEntry.getValue());
                        }
                    }
                    properties.put(id, specificPropertyMap);
                });
            }
        });
    }

    @Override
    public String getKey() {
        return key;
    }

    public boolean generateConverters() {
        return generateConvertersOptional.orElse(MiapiConfig.INSTANCE.server.generatedMaterials.defaultGenerateConverters);
    }

    @Override
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        groups.add(key);
        try {
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
        } catch (RuntimeException e) {
            Miapi.LOGGER.warn("Groups were not correctly set up in json Material!" + getKey() + " " + rawJson);
        }
        return groups;
    }

    @Override
    public List<String> getGuiGroups() {
        List<String> groups = new ArrayList<>();
        groups.add(key);
        try {
            if (rawJson.getAsJsonObject().has("groups")) {
                JsonArray groupsJson = rawJson.getAsJsonObject().getAsJsonArray("groups");
                for (JsonElement groupElement : groupsJson) {
                    String group = groupElement.getAsString();
                    groups.add(group);
                }
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.warn("Groups were not correctly set up in json Material!" + getKey() + " " + rawJson);
        }
        return groups;
    }

    @Override
    public Map<ModuleProperty, JsonElement> materialProperties(String key) {
        return propertyMap.getOrDefault(key, new HashMap<>());
    }

    @Override
    public Map<ModuleProperty, JsonElement> getDisplayMaterialProperties(String key) {
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

    @Override
    public List<String> getTextureKeys() {
        List<String> textureKeys = new ArrayList<>();
        if (rawJson.getAsJsonObject().has("textures")) {
            try {
                JsonArray textures = rawJson.getAsJsonObject().getAsJsonArray("textures");
                for (JsonElement texture : textures) {
                    textureKeys.add(texture.getAsString());
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("textures in material " + getKey() + " is not setup correctly");
            }
        }
        textureKeys.add("default");
        return new ArrayList<>(textureKeys);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public int getColor() {
        if (rawJson.getAsJsonObject().get("color") != null) {
            try {
                long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
                return (int) (longValue & 0xffffffffL);
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("textures in material " + getKey() + " is not setup correctly");
            }
        }
        return getRenderController().getAverageColor().argb();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public MaterialRenderController getRenderController() {
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

    Ingredient matching;

    @Override
    public double getValueOfItem(ItemStack item) {
        JsonArray items = rawJson.getAsJsonObject().getAsJsonArray("items");
        for (JsonElement element : items) {
            JsonObject itemObj = element.getAsJsonObject();

            if (itemObj.has("item")) {
                String itemId = itemObj.get("item").getAsString();
                if (Registries.ITEM.getId(item.getItem()).toString().equals(itemId)) {
                    try {
                        Item item1 = Registries.ITEM.get(new Identifier(itemId));
                        matching = Ingredient.ofItems(item1);
                        return itemObj.get("value").getAsDouble();
                    } catch (Exception surpressed) {
                        return 1;
                    }
                }
            } else if (itemObj.has("tag")) {
                String tagId = itemObj.get("tag").getAsString();
                TagKey<Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
                matching = Ingredient.fromTag(tag);
                if (tag != null && item.isIn(tag)) {
                    try {
                        return itemObj.get("value").getAsDouble();
                    } catch (Exception suppressed) {
                        return 1;
                    }
                }
            } else if (itemObj.has("ingredient")) {
                Ingredient ingredient = Ingredient.fromJson(itemObj.get("ingredient"));
                matching = ingredient;
                if (ingredient.test(item)) {
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
    public Ingredient getIngredient() {
        if (matching == null) {
            getValueOfItem(ItemStack.EMPTY);
        }
        return matching;
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
