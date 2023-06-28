package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.synergies.SynergyManager;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class Skin {
    public String path;
    public ItemModule module;
    public ModuleCondition condition;
    public Map<ModuleProperty, JsonElement> properties = new HashMap<>();
    public TextureOptions textureOptions = new TextureOptions(new Identifier(Miapi.MOD_ID, "textures/gui/skin/skin_button.png"), 100, 16, 3, ColorHelper.Argb.getArgb(255, 255, 255, 255), 1);


    public static Skin fromJson(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        Skin skin = new Skin();
        skin.module = RegistryInventory.modules.get(jsonObject.get("module").getAsString());
        skin.condition = ConditionManager.get(jsonObject.get("condition"));
        skin.properties = SynergyManager.getProperties(jsonObject.get("properties"));
        skin.path = jsonObject.get("path").getAsString();
        skin.textureOptions = TextureOptions.fromJson(jsonObject.get("texture"), new Identifier(Miapi.MOD_ID, "textures/gui/skin/skin_button.png"), 100, 16, 3, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        return skin;
    }
}
