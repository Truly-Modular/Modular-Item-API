package smartin.miapi.modules.properties.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientJsonMaterial extends JsonMaterial{
    @Nullable
    public MaterialIcons.MaterialIcon icon;
    protected smartin.miapi.modules.properties.material.palette.MaterialPalette palette;

    public ClientJsonMaterial(JsonObject element) {
        super(element);
        if (element.has("icon") ) {
            JsonElement emnt = element.get("icon");
            if (emnt instanceof JsonPrimitive primitive && primitive.isString())
                icon = new MaterialIcons.TextureMaterialIcon(new Identifier(primitive.getAsString()));
            else icon = MaterialIcons.getMaterialIcon(key, emnt);
        }

        if (element.has("color_palette")) {
            palette = smartin.miapi.modules.properties.material.palette.PaletteCreators.paletteCreator.dispatcher().createPalette(element.get("color_palette"), this);
        } else {
            palette = new smartin.miapi.modules.properties.material.palette.EmptyMaterialPalette(this);
        }
    }

    @Override
    public smartin.miapi.modules.properties.material.palette.MaterialPalette getPalette() {
        return palette;
    }

    public int renderIcon(DrawContext drawContext, int x, int y) {
        if (icon == null) return 0;
        return icon.render(drawContext, x, y);
    }

    public boolean hasIcon() {
        return icon != null;
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
    public int getColor() { // TODO getPalette().getPaletteAverageColor() ?
        if (rawJson.getAsJsonObject().get("color") != null) {
            long longValue = Long.parseLong(rawJson.getAsJsonObject().get("color").getAsString(), 16);
            return (int) (longValue & 0xffffffffL);
        }
        return ColorHelper.Argb.getArgb(255, 255, 255, 255);
    }
}
