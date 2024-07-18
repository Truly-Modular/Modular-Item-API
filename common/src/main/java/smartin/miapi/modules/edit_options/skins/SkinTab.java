package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.FastColor;
import smartin.miapi.Miapi;

public class SkinTab {
    public TextureOptions header;
    public TextureOptions side;
    public TextureOptions background;
    public String path;


    public static SkinTab fromJson(JsonElement element) {
        JsonObject jsonObject = element == null ? new JsonObject() : element.getAsJsonObject();
        SkinTab tab = new SkinTab();
        tab.header = TextureOptions.fromJson(jsonObject.get("header"), Miapi.id("textures/gui/skin/tab_header.png"), 100, 14, 3, FastColor.ARGB32.color(255, 255, 255, 255));
        tab.side = TextureOptions.fromJson(jsonObject.get("side"), Miapi.id("textures/gui/skin/tab_side.png"), 14, 30, 0, FastColor.ARGB32.color(255, 255, 255, 255));
        tab.background = TextureOptions.fromJson(jsonObject.get("background"), Miapi.id("textures/gui/skin/tab_background.png"), 100, 100, 3, FastColor.ARGB32.color(255, 255, 255, 255));
        tab.path = element == null ? "" : jsonObject.get("path").getAsString();
        return tab;
    }
}
