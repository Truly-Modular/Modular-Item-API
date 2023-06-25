package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.conditions.ModuleCondition;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.synergies.SynergyManager;

import java.util.HashMap;
import java.util.Map;

public class SkinTab {
    public TextureOptions header;
    public TextureOptions side;
    public TextureOptions background;
    public String path;


    public static SkinTab fromJson(JsonElement element) {
        JsonObject jsonObject = element==null ? new JsonObject() :  element.getAsJsonObject() ;
        SkinTab tab = new SkinTab();
        tab.header = smartin.miapi.modules.edit_options.skins.TextureOptions.fromJson(jsonObject.get("header"), new Identifier(Miapi.MOD_ID, "textures/gui/skin/tab_header.png"), 100, 14, 3, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        tab.side = smartin.miapi.modules.edit_options.skins.TextureOptions.fromJson(jsonObject.get("side"), new Identifier(Miapi.MOD_ID, "textures/gui/skin/tab_side.png"), 14, 30, 0, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        tab.background = smartin.miapi.modules.edit_options.skins.TextureOptions.fromJson(jsonObject.get("background"), new Identifier(Miapi.MOD_ID, "textures/gui/skin/tab_background.png"), 100, 100, 3, ColorHelper.Argb.getArgb(255, 255, 255, 255));
        tab.path = element==null ? "" : jsonObject.get("path").getAsString();
        return tab;
    }
}
