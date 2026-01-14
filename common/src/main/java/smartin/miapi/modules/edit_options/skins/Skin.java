package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.PropertyHolder;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.tag.ModuleTagProperty;
import smartin.miapi.registries.JsonOpsBooleanPatched;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Skin {
    public String path;
    public String modID;
    public String type;
    public ItemModule module;
    public ModuleCondition condition;
    public PropertyHolder propertyHolder;
    public TextureOptions textureOptions = new TextureOptions(Miapi.id("textures/gui/skin/skin_button.png"), 100, 16, 3, FastColor.ARGB32.color(255, 255, 255, 255), 1, false);
    @Nullable
    public Component hoverDescription;


    public static List<Skin> fromJson(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        List<Skin> skins = new ArrayList<>();
        getModules(element).forEach(itemModule -> {
            Skin skin = new Skin();
            skin.module = itemModule;
            skin.condition = ConditionManager.get(jsonObject.get("condition"));
            skin.path = jsonObject.get("path").getAsString();
            skin.propertyHolder = PropertyHolder.MAP_CODEC.codec().decode(JsonOpsBooleanPatched.INSTANCE, jsonObject).getOrThrow((s) -> new DecoderException("Failed to decode skin !" + s)).getFirst();
            skin.textureOptions = TextureOptions.fromJson(jsonObject.get("texture"), Miapi.id("textures/gui/skin/skin_button.png"), 100, 16, 3, FastColor.ARGB32.color(255, 255, 255, 255));
            if (jsonObject.has("hover")) {
                skin.hoverDescription = ComponentSerialization.CODEC.parse(
                        JsonOpsBooleanPatched.INSTANCE,
                        jsonObject.getAsJsonObject("hover")).result().orElse(Component.empty());
            }
            if (jsonObject.has("type")) {
                skin.type = jsonObject.get("type").getAsString();
            } else {
                skin.type = "model";
            }
            skins.add(skin);
        });
        return skins;
    }

    public static List<Skin> getSkins(ModuleInstance moduleInstance) {
        JsonElement element = moduleInstance.moduleData.get(Miapi.id("skin"));
        List<Skin> result = new ArrayList<>();

        if (element != null) {
            Map<String, Skin> moduleSkins = SkinOptions.skins.get(moduleInstance.getModule().id());
            if (moduleSkins == null) {
                return result;
            }

            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                // Backwards compatibility: single skin
                Skin skin = moduleSkins.get(element.getAsString());
                if (skin != null) {
                    result.add(skin);
                }
            } else if (element.isJsonArray()) {
                for (JsonElement item : element.getAsJsonArray()) {
                    if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isString()) {
                        Skin skin = moduleSkins.get(item.getAsString());
                        if (skin != null) {
                            result.add(skin);
                        }
                    }
                }
            }
        }

        return result;
    }


    public static void writeSkins(ModuleInstance moduleInstance, List<Skin> skinKeys) {
        JsonArray array = new JsonArray();
        for (Skin key : skinKeys) {
            if (key != null && key.path != null) {
                array.add(new JsonPrimitive(key.path));
            }
        }
        moduleInstance.moduleData.put(Miapi.id("skin"), array);
    }

    public static void writeSkinsKeys(ModuleInstance moduleInstance, List<String> skinKeys) {
        JsonArray array = new JsonArray();
        for (String key : skinKeys) {
            array.add(new JsonPrimitive(key));
        }
        moduleInstance.moduleData.put(Miapi.id("skin"), array);
    }


    public static List<ItemModule> getModules(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        List<ItemModule> modules = new ArrayList<>();
        if (jsonObject.has("module_tags")) {
            jsonObject.get("module_tags").getAsJsonArray().asList().forEach(jsonElement -> {
                modules.addAll(ModuleTagProperty.getModulesWithTag(jsonElement.getAsString()));
            });
        }
        if (jsonObject.has("module")) {
            JsonElement moduleElement = jsonObject.get("module");
            if (moduleElement.isJsonArray()) {
                jsonObject.get("module").getAsJsonArray().asList().forEach(jsonElement -> {
                    ItemModule itemModule = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(jsonElement.getAsString());
                    if (itemModule != null) {
                        modules.add(itemModule);
                    }
                });
            } else {
                ItemModule itemModule = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(jsonObject.get("module").getAsString());
                if (itemModule != null) {
                    modules.add(itemModule);
                }
            }
        }
        return modules;
    }
}
