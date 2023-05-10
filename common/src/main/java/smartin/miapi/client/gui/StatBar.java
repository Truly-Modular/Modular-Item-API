package smartin.miapi.client.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

public class StatBar extends InteractAbleWidget {
    double primaryPercent = 0;
    double secondaryPercent = 0;
    int primaryColor = 0;
    int secondaryColor = 0;
    int offColor = 0;

    public StatBar(int x, int y, int width, int height, int offColor) {
        super(x, y, width, height, Text.empty());
        this.offColor = offColor;
    }

    public void setPrimary(double primaryPercent, int color) {
        primaryPercent = Math.min(1, Math.max(0, primaryPercent));
        this.primaryColor = color;
        this.primaryPercent = primaryPercent;
    }

    public void setSecondary(double secondaryPercent, int color) {
        secondaryPercent = Math.min(1, Math.max(0, secondaryPercent));
        this.secondaryColor = color;
        this.secondaryPercent = secondaryPercent;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        fill(matrices, x, y, x + width, y + height, ColorHelper.Argb.getArgb(255, 255, 0, 255));
        fill(matrices, x, y, (int) (x + width * primaryPercent), height + y, primaryColor);
        fill(matrices, (int) (x + width * primaryPercent), y, (int) (x + width * secondaryPercent), height + y, secondaryColor);
        fill(matrices, (int) (x + width * secondaryPercent), y, x + width, height + y, offColor);

    }
}
