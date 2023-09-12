package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public interface Material {
    Identifier baseColorPalette = new Identifier(Miapi.MOD_ID, "textures/miapi_materials/base_palette.png");

    String getKey();

    static int getColor(String color) {
        if (color.equals("")) return ColorHelper.Argb.getArgb(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    List<String> getGroups();

    @Environment(EnvType.CLIENT)
    @Nullable
    SpriteContents generateSpriteContents();

    @Environment(EnvType.CLIENT)
    @Nullable
    Identifier getSpriteId();

    /*@Environment(EnvType.CLIENT)
    VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader);

    @Environment(EnvType.CLIENT)
    static VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader, Identifier texture) {
        int id = 10;
        RenderSystem.setShaderTexture(id, texture);
        RenderSystem.bindTexture(id);
        int j = RenderSystem.getShaderTexture(id);
        shader.addSampler("MatColors", j);
        return provider.getBuffer(layer);
    }*/

    /**
     * @param drawContext a DrawContext that can be used to draw shtuff
     * @param x x pos of the icon
     * @param y y pos of the icon
     * @return how much to offset the text rendering by
     */
    @Environment(EnvType.CLIENT)
    default int renderIcon(DrawContext drawContext, int x, int y) {
        return 0;
    }

    @Environment(EnvType.CLIENT)
    default boolean hasIcon() {
        return false;
    }

    Map<ModuleProperty, JsonElement> materialProperties(String key);

    JsonElement getRawElement(String key);

    double getDouble(String property);

    String getData(String property);

    List<String> getTextureKeys();

    int getColor();

    double getValueOfItem(ItemStack item);
}
