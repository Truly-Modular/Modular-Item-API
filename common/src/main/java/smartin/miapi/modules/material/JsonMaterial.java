package smartin.miapi.modules.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.palette.FallbackColorer;
import smartin.miapi.modules.material.palette.MaterialRenderController;
import smartin.miapi.modules.material.palette.MaterialRenderControllers;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.FakeTranslation;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * quite frankly, this isnt a good implementation. its still very much based on JSON instead of normal codecs.
 * Its a functional implementation of a Material based on a Raw JsonElement.
 */
public class JsonMaterial implements Material {
    public ResourceLocation id;
    protected JsonElement rawJson;
    @Nullable
    public MaterialIcons.MaterialIcon icon;
    protected MaterialRenderController palette;
    public Map<String, Map<ModuleProperty<?>, Object>> propertyMap = new HashMap<>();
    public Map<String, Map<ModuleProperty<?>, Object>> displayPropertyMap = new HashMap<>();
    TagKey<Block> incorrectForTool = BlockTags.INCORRECT_FOR_WOODEN_TOOL;

    public JsonMaterial(ResourceLocation id, JsonObject element, boolean isClient) {
        rawJson = element;
        this.id = id;

        if (isClient) {
            if (element.has("icon")) {
                JsonElement emnt = element.get("icon");
                if (emnt instanceof JsonPrimitive primitive && primitive.isString())
                    icon = new MaterialIcons.TextureMaterialIcon(ResourceLocation.parse(primitive.getAsString()));
                else icon = MaterialIcons.getMaterialIcon(this.id, emnt);
            }

            if (element.has("color_palette")) {
                JsonElement innerElement = element.get("color_palette");
                palette = MaterialRenderControllers.creators.get(innerElement.getAsJsonObject().get("type").getAsString()).createPalette(innerElement, this);
            } else {
                palette = new FallbackColorer(this);
            }
            if (element.has("fake_translation") && element.has("translation")) {
                FakeTranslation.translations.put(element.get("translation").getAsString(), element.get("fake_translation").getAsString());
            }
            if (getTranslation().getString().contains(".")) {
                Miapi.LOGGER.error("Material " + getID().toString() + " likely has a broken Translation!");
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
                case "mining_level": {
                    ResourceLocation id = ResourceLocation.CODEC.decode(JsonOps.INSTANCE, propertyElement).getOrThrow().getFirst();
                    var found = BuiltInRegistries.BLOCK.getTags().filter(pair -> pair.getFirst().location().equals(id)).findAny();
                    found.ifPresent(tagKeyNamedPair -> incorrectForTool = tagKeyNamedPair.getFirst());
                    break;
                }
                case "hidden_properties": {
                    mergeProperties(propertyElement, propertyMap);
                    break;
                }
                case "color_palette": {
                    if (isClient) {
                        palette = MaterialRenderControllers.creators.get(
                                propertyElement.getAsJsonObject().get("type").getAsString()).createPalette(propertyElement, this);
                    }
                    break;
                }
                case "icon": {
                    if (isClient) {
                        JsonElement emnt = propertyElement;
                        if (emnt instanceof JsonPrimitive primitive && primitive.isString())
                            icon = new MaterialIcons.TextureMaterialIcon(ResourceLocation.parse(primitive.getAsString()));
                        else icon = MaterialIcons.getMaterialIcon(id, emnt);
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

    private static void mergeProperties(JsonElement propertyElement, Map<String, Map<ModuleProperty<?>, Object>> properties) {
        propertyElement.getAsJsonObject().asMap().forEach((id, element) -> {
            if (element != null) {
                element.getAsJsonObject().entrySet().forEach(stringJsonElementEntry -> {
                    ModuleProperty<?> property = RegistryInventory.moduleProperties.get(Miapi.id(stringJsonElementEntry.getKey()));
                    Map<ModuleProperty<?>, Object> specificPropertyMap = properties.getOrDefault(id, new HashMap<>());
                    if (property != null) {
                        try {
                            if (specificPropertyMap.containsKey(property)) {
                                specificPropertyMap.put(property, ItemModule.merge(
                                        property,
                                        specificPropertyMap.get(property),
                                        property.decode(stringJsonElementEntry.getValue()),
                                        MergeType.SMART));
                            } else {
                                specificPropertyMap.put(property, property.decode(stringJsonElementEntry.getValue()));
                            }
                        } catch (Exception e) {
                            Miapi.LOGGER.error("Could not load property in material :", e);
                        }
                    } else {
                        Miapi.LOGGER.error("Could not find property " + stringJsonElementEntry.getKey());
                    }
                    properties.put(id, specificPropertyMap);
                });
            }
        });
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    @Override
    public List<String> getGroups() {
        List<String> groups = new ArrayList<>();
        groups.add(id.toString());
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
        groups.add(id.toString());
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
            try {
                return jsonData.getAsDouble();
            } catch (Exception e) {
                Miapi.LOGGER.error("Material data " + property + " was not a Number!" + jsonData);
            }
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
    public int renderIcon(GuiGraphics drawContext, int x, int y) {
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
                if (BuiltInRegistries.ITEM.getKey(item.getItem()).toString().equals(itemId)) {
                    try {
                        return itemObj.get("value").getAsDouble();
                    } catch (Exception surpressed) {
                        return 1;
                    }
                }
            } else if (itemObj.has("tag")) {
                String tagId = itemObj.get("tag").getAsString();
                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagId));
                if (tag != null && item.is(tag)) {
                    try {
                        return itemObj.get("value").getAsDouble();
                    } catch (Exception suppressed) {
                        return 1;
                    }
                }
            } else if (itemObj.has("ingredient")) {
                Ingredient ingredient = Ingredient.CODEC.decode(JsonOps.INSTANCE, itemObj.get("ingredient")).getOrThrow().getFirst();
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
    public Double getPriorityOfIngredientItem(ItemStack itemStack) {
        if (this.getRawElement("items") != null && this.getRawElement("items").isJsonArray()) {
            JsonArray items = this.getRawElement("items").getAsJsonArray();

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("item")) {
                    String itemId = itemObj.get("item").getAsString();
                    if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().equals(itemId)) {
                        return 0.0;
                    }
                }
            }

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("ingredient")) {
                    Ingredient ingredient = Ingredient.CODEC.decode(JsonOps.INSTANCE, itemObj.get("ingredient")).getOrThrow().getFirst();
                    if (ingredient.test(itemStack)) {
                        return 5.0;
                    }
                }
            }

            for (JsonElement element : items) {
                JsonObject itemObj = element.getAsJsonObject();

                if (itemObj.has("tag")) {
                    String tagId = itemObj.get("tag").getAsString();
                    TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(tagId));
                    if (tag != null && itemStack.is(tag)) {
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

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectForTool;
    }
}
