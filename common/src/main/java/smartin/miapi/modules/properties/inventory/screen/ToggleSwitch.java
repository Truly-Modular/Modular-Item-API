package smartin.miapi.modules.properties.inventory.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * A small 45x9 toggle switch.
 *
 * The first 45 pixels are interactive.
 * The remaining 10 pixels are intentional dead space.
 */
public class ToggleSwitch extends InventoryTypeConfigElement {

    private static final int TOGGLE_WIDTH = 35;
    private static final int TOGGLE_HEIGHT = 9;
    private static final int CLICK_GAP = 10;

    private static final int TEXTURE_X = 193;
    private static final int OFF_TEXTURE_Y = 0;
    private static final int ON_TEXTURE_Y = 9;

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("miapi", "textures/gui/inventory.png");

    private boolean toggled;
    private final Component tooltipOn;
    private final Component tooltipOff;
    private final Consumer<Boolean> toggleCallback;

    public ToggleSwitch(
            int x,
            int y,
            boolean initialState,
            Component tooltipOn,
            Component tooltipOff,
            Consumer<Boolean> toggleCallback
    ) {
        // 35px visible switch + 10px non-interactive gap.
        super(
                x,
                y,
                TOGGLE_HEIGHT,
                Component.empty()
        );

        this.toggled = initialState;
        this.tooltipOn = tooltipOn;
        this.tooltipOff = tooltipOff;
        this.toggleCallback = toggleCallback;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int textureY = toggled ? ON_TEXTURE_Y : OFF_TEXTURE_Y;

        graphics.blit(
                TEXTURE,
                getX(),
                getY(),
                TEXTURE_X,
                textureY,
                TOGGLE_WIDTH,
                TOGGLE_HEIGHT,
                512,
                512
        );
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Do not allow the 10px gap to activate the switch.
        if (mouseX < getX() || mouseX >= getX() + TOGGLE_WIDTH) {
            return;
        }

        toggled = !toggled;
        toggleCallback.accept(toggled);
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }


    public Component getHOverTooltip() {
        return toggled ? tooltipOn : tooltipOff;
    }
}