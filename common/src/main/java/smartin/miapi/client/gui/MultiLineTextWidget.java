package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A configurable Multiline Textwidget
 */
@Environment(EnvType.CLIENT)
public class MultiLineTextWidget extends InteractAbleWidget {

    public Component rawText;
    public List<Component> lines = new ArrayList<>();
    private int longestLine;
    public int maxLineLength;
    public Font textRenderer = Minecraft.getInstance().font;
    public int spacer = 1;
    public boolean hasTextShadow = true;
    public int textColor = FastColor.ARGB32.color(255, 255, 255, 255);


    public MultiLineTextWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, Component.empty());
        maxLineLength = width;
        setText(text);
    }

    public void setText(Component text) {
        rawText = text;
        List<String> rawLines = Arrays.stream(text.getString().split("\n")).collect(Collectors.toList());
        lines.clear();

        while (!rawLines.isEmpty()) {
            String rawLine = rawLines.remove(0);
            if (maxLineLength > 0 && textRenderer.width(rawLine) > maxLineLength) {
                List<String> words = new ArrayList<>(Arrays.stream(rawLine.split(" ")).toList());
                StringBuilder currentLine = new StringBuilder();
                currentLine.append(words.remove(0));
                currentLine.append(" ");
                StringBuilder nextLine = new StringBuilder();
                int currentLineLength = 0;

                boolean isLineExceeded = false;
                for (String word : words) {
                    int wordLength = textRenderer.width(word);

                    if ((!isLineExceeded && currentLineLength + wordLength + 1 <= maxLineLength)) {
                        // Include the word in the current line if it's the first word or if the line has already exceeded the maximum length
                        currentLine.append(word).append(" ");
                        currentLineLength = textRenderer.width(currentLine.toString());
                    } else {
                        // Add the word to the next line if it exceeds the maximum length
                        nextLine.append(word).append(" ");
                        isLineExceeded = true;
                    }
                }
                if (isLineExceeded) {
                    rawLines.add(0, nextLine.toString());
                }
                rawLine = currentLine.toString().trim();
            }

            Component line = Component.translatable(rawLine);
            int size = textRenderer.width(line);
            if (size > longestLine) {
                longestLine = size;
            }
            lines.add(line);
        }
        this.width = longestLine;
        this.height = lines.size() * textRenderer.lineHeight + Math.max(0, lines.size() - 1) * spacer;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int start = getY();
        for (Component line : lines) {
            context.drawString(textRenderer, line, getX(), start, textColor, hasTextShadow);
            start += textRenderer.lineHeight + spacer;
        }
        super.renderWidget(context, mouseX, mouseY, delta);
    }
}
