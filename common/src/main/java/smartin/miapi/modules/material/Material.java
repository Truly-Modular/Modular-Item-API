package smartin.miapi.modules.material;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.palette.MaterialPalette;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;

import static smartin.miapi.Miapi.MOD_ID;

public interface Material {
    Identifier BASE_PALETTE_ID = new Identifier(MOD_ID, "miapi_materials/base_palette");

    String getKey();

    @Environment(EnvType.CLIENT)
    static int getColor(String color) {
        if (color.equals("")) return ColorHelper.Argb.getArgb(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    List<String> getGroups();

    @Environment(EnvType.CLIENT)
    MaterialPalette getPalette();

    @Environment(EnvType.CLIENT)
    default VertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack, ItemModule.ModuleInstance moduleInstance, ModelTransformationMode mode) {
        return getPalette().getVertexConsumer(vertexConsumers, stack, moduleInstance, mode);
        //return new MaterialVertexConsumer(vertexConsumers.getBuffer(RegistryInventory.Client.entityTranslucentMaterialRenderType), this);
    }

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

    default Material getMaterial(ItemModule.ModuleInstance moduleInstance){
        return this;
    }

    default Material getMaterialFromIngredient(ItemStack ingredient){
        return this;
    }

    @Environment(EnvType.CLIENT)
    default boolean hasIcon() {
        return false;
    }

    Map<ModuleProperty, JsonElement> materialProperties(String key);

    List<String> getAllPropertyKeys();

    double getDouble(String property);

    String getData(String property);

    List<String> getTextureKeys();

    @Environment(EnvType.CLIENT)
    default int getColor() {
        return getPalette().getPaletteAverageColor().argb();
    }

    double getValueOfItem(ItemStack item);

    /**
     * return null if itemstack is not assosiated with the material
     */
    @Nullable
    Double getPriorityOfIngredientItem(ItemStack itemStack);
}
