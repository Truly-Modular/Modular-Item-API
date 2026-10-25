package smartin.miapi.modules.edit_options;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface EditOptionRender {
    /**
     * always expect 18x18 size for this. render the icon
     */
    @Environment(EnvType.CLIENT)
    void renderIcon(GuiGraphics drawContext, boolean isSelected, int x, int y, float delta);

    Component getHoverName();
}
