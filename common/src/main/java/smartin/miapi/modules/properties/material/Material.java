package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

public interface Material {
    Identifier baseColorPalette = new Identifier(Miapi.MOD_ID, "textures/item/materials/base_palette.png");

    String getKey();

    static int getColor(String color) {
        if (color.equals("")) return ColorHelper.Argb.getArgb(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    List<String> getGroups();

    VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader);

    static VertexConsumer setupMaterialShader(VertexConsumerProvider provider, RenderLayer layer, ShaderProgram shader, Identifier texture) {
        int id = 10;
        RenderSystem.setShaderTexture(id, texture);
        RenderSystem.bindTexture(id);
        int j = RenderSystem.getShaderTexture(id);
        shader.addSampler("MatColors", j);
        return provider.getBuffer(layer);
    }

    Map<ModuleProperty, JsonElement> materialProperties(String key);

    JsonElement getRawElement(String key);

    double getDouble(String property);

    String getData(String property);

    List<String> getTextureKeys();

    int getColor();

    double getValueOfItem(ItemStack item);
}
