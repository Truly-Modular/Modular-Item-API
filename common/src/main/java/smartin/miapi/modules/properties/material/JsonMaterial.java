package smartin.miapi.modules.properties.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonMaterial implements Material {
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

    @Override
    public int getColor() {
        if (rawJson.getAsJsonObject().get("color") != null) {
            long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
            return (int) (longValue & 0xffffffffL);
        }
        return ColorHelper.Argb.getArgb(255, 255, 255, 255);
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
