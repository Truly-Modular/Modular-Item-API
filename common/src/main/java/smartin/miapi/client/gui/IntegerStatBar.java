package smartin.miapi.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

public class IntegerStatBar extends InteractAbleWidget {
    int primaryValue = 0;
    int secondaryValue = 0;
    int maxSteps = 1;
    int primaryColor = 1;
    int secondaryColor = 1;
    int offColor = ColorHelper.Argb.getArgb(255,0,0,0);
    int gapWidth = 1;

    public IntegerStatBar(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = Math.max(1, maxSteps);
    }

    public void setPrimary(int currentValue, int color) {
        this.primaryColor = color;
        this.primaryValue = currentValue;
    }

    public void setSecondary(int secondaryValue, int color) {
        this.secondaryColor = color;
        this.secondaryValue = secondaryValue;
    }

    public void setGapWidth(int gapWidth) {
        this.gapWidth = Math.max(0, gapWidth);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int newMaxSteps = Math.max(this.maxSteps,Math.max(this.primaryValue,this.secondaryValue));

        int stepWidth = (int) ((double) (width - gapWidth * (newMaxSteps - 1)) / newMaxSteps);

        for (int i = 0; i < newMaxSteps; i++) {
            int segmentX = getX() + (stepWidth + gapWidth) * i;
            int segmentEndX = segmentX + stepWidth;

            if (i < primaryValue) {
                context.fill(segmentX, getY(), segmentEndX, height + getY(), primaryColor);
            } else if (i < secondaryValue) {
                context.fill(segmentX, getY(), segmentEndX, height + getY(), secondaryColor);
            } else {
                context.fill(segmentX, getY(), segmentEndX, height + getY(), offColor);
            }
        }
    }
}