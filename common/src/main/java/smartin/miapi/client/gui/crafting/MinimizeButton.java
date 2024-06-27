package smartin.miapi.client.gui.crafting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import smartin.miapi.client.gui.InteractAbleWidget;

@Environment(EnvType.CLIENT)
public class MinimizeButton extends InteractAbleWidget {
    private final Runnable onMinimized;
    private final Runnable onMaximized;
    private boolean isEnabled = true;
    private long timeClicked = -100;

    public MinimizeButton(int x, int y, int width, int height, Runnable onMinimized, Runnable onMaximized) {
        super(x, y, width, height, Component.empty());
        this.onMinimized = onMinimized;
        this.onMaximized = onMaximized;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            playClickedSound();
            if (isEnabled) minimize();
            else maximize();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void maximize() {
        if (!isEnabled) {
            timeClicked = Util.getMillis();
            onMaximized.run();
            isEnabled = true;
        }
    }
    public void minimize() {
        if (isEnabled) {
            timeClicked = Util.getMillis();
            onMinimized.run();
            isEnabled = false;
        }
    }
    public long getLastChangeTime() {
        return timeClicked;
    }

    @Override
    public void renderWidget(GuiGraphics drawContext, int mouseX, int mouseY, float delta) {
        //drawContext.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 10, new Color(255, 0, 0, 255).argb());
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
