package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.TagProperty;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class Skin {
    public String path;
    public ItemModule module;
    public ModuleCondition condition;
    public SynergyManager.PropertyHolder propertyHolder;
    public TextureOptions textureOptions = new TextureOptions(new ResourceLocation(Miapi.MOD_ID, "textures/gui/skin/skin_button.png"), 100, 16, 3, FastColor.ARGB32.color(255, 255, 255, 255), 1, false);
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
            skin.propertyHolder = SynergyManager.getFrom(jsonObject, "skin for " + skin.module + " skinpath " + skin.path);
            skin.textureOptions = TextureOptions.fromJson(jsonObject.get("texture"), new ResourceLocation(Miapi.MOD_ID, "textures/gui/skin/skin_button.png"), 100, 16, 3, FastColor.ARGB32.color(255, 255, 255, 255));
            if (jsonObject.has("hover")) {
                skin.hoverDescription = Codecs.TEXT.parse(
                        JsonOps.INSTANCE,
                        jsonObject.getAsJsonObject("hover")).result().orElse(Component.empty());
            }
            skins.add(skin);
        });
        return skins;
    }

    public static List<ItemModule> getModules(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        List<ItemModule> modules = new ArrayList<>();
        if (jsonObject.has("module_tags")) {
            jsonObject.get("module_tags").getAsJsonArray().asList().forEach(jsonElement -> {
                modules.addAll(TagProperty.getModulesWithTag(jsonElement.getAsString()));
            });
        }
        if (jsonObject.has("module")) {
            JsonElement moduleElement = jsonObject.get("module");
            if (moduleElement.isJsonArray()) {
                jsonObject.get("module").getAsJsonArray().asList().forEach(jsonElement -> {
                    ItemModule itemModule = RegistryInventory.modules.get(jsonElement.getAsString());
                    if (itemModule != null) {
                        modules.add(itemModule);
                    }
                });
            } else {
                ItemModule itemModule = RegistryInventory.modules.get(jsonObject.get("module").getAsString());
                if (itemModule != null) {
                    modules.add(itemModule);
                }
            }
        }
        return modules;
    }
}
