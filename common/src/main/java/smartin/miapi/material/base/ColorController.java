package smartin.miapi.material.base;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import smartin.miapi.material.palette.MaterialRenderController;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

/**
 * Controls most of the Client side rendering controls of Materials.
 * Refactored into this class for an easier overview
 */
public interface ColorController {

    @Environment(EnvType.CLIENT)
    default boolean hasIcon() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    static int getColor(String color) {
        if (color.equals("")) return FastColor.ARGB32.color(255, 255, 255, 255);
        long longValue = Long.parseLong(color, 16);
        return (int) (longValue & 0xffffffffL);
    }

    /**
     * a simplified integer color of the material, used for fallback rendering
     *
     * @return
     */
    @Environment(EnvType.CLIENT)
    default int getColor(ModuleInstance context) {
        return getColor(context, ItemDisplayContext.GUI);
    }

    /**
     * a simplified integer color of the material, used for fallback rendering
     *
     * @return
     */
    @Environment(EnvType.CLIENT)
    default int getColor(ModuleInstance context, ItemDisplayContext mode) {
        return getRenderController(context, mode).getAverageColor().argb();
    }

    /**
     * the texturekeys in order of the material, some module might offer multiple variants, this is to access them
     *
     * @return
     */
    List<String> getTextureKeys();

    @Environment(EnvType.CLIENT)
    MaterialRenderController getRenderController(ModuleInstance context, ItemDisplayContext mode);

    /**
     * @param drawContext a DrawContext that can be used to draw shtuff
     * @param x           x pos of the icon
     * @param y           y pos of the icon
     * @return how much to offset the text rendering by
     */
    @Environment(EnvType.CLIENT)
    default int renderIcon(GuiGraphics drawContext, int x, int y) {
        return 0;
    }
}
