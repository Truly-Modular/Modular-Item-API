package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public record TextureOptions(ResourceLocation texture, int xSize, int ySize, int borderSize, int color, int scale,
                             boolean keepScale) {

    public static TextureOptions fromJson(JsonElement element, ResourceLocation texture, int xSize, int ySize, int borderSize, int color) {
        if (element == null) {
            return new TextureOptions(texture, xSize, ySize, borderSize, color, 1, false);
        }
        JsonObject jsonObject = element.getAsJsonObject();

        ResourceLocation textureValue = jsonObject.has("texture") ? new ResourceLocation(jsonObject.get("texture").getAsString()) : texture;
        int xSizeValue = jsonObject.has("xSize") ? jsonObject.get("xSize").getAsInt() : xSize;
        int ySizeValue = jsonObject.has("ySize") ? jsonObject.get("ySize").getAsInt() : ySize;
        int borderSizeValue = jsonObject.has("borderSize") ? jsonObject.get("borderSize").getAsInt() : borderSize;
        if (jsonObject.has("color")) {
            long longValue = Long.parseLong(jsonObject.get("color").getAsString(), 16);
            color = (int) (longValue & 0xffffffffL);
        }
        int scale = jsonObject.has("scale") ? jsonObject.get("scale").getAsInt() : 1;
        boolean keepScale = jsonObject.has("keepScale") && jsonObject.get("keepScale").getAsBoolean();
        return new TextureOptions(textureValue, xSizeValue, ySizeValue, borderSizeValue, color, scale, keepScale);
    }
}