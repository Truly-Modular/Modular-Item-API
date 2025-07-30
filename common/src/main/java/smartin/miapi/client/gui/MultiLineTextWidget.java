package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import smartin.miapi.config.MiapiConfig;

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
                List<String> words;
                if (MiapiConfig.getClientConfig().other.splitNewLineAlways) {
                    words = rawLine.chars()
                            .mapToObj(c -> String.valueOf((char) c))
                            .collect(Collectors.toList());
                } else {
                    words = new ArrayList<>(Arrays.stream(rawLine.split(" ")).toList());
                }
                StringBuilder currentLine = new StringBuilder();
                currentLine.append(words.remove(0));
                if (!MiapiConfig.getClientConfig().other.splitNewLineAlways) {
                    currentLine.append(" ");
                }
                StringBuilder nextLine = new StringBuilder();
                int currentLineLength = 0;

                boolean isLineExceeded = false;
                for (String word : words) {
                    int wordLength = textRenderer.width(word);

                    if (wordLength > maxLineLength) {
                        // Word too long and can't fit on an empty line: split by characters
                        StringBuilder part = new StringBuilder();
                        for (char c : word.toCharArray()) {
                            part.append(c);
                            if (textRenderer.width(part.toString()) > maxLineLength) {
                                // Push the previous part (excluding this char) as a new line
                                rawLines.add(0, word.substring(part.length() - 1)); // remainder
                                word = part.substring(0, part.length() - 1);
                                break;
                            }
                        }
                        rawLines.add(0, word); // re-add the split word
                        break; // Restart processing with the split word
                    }else if ((!isLineExceeded && currentLineLength + wordLength + 1 <= maxLineLength)) {
                        currentLine.append(word);
                        if (!MiapiConfig.getClientConfig().other.splitNewLineAlways) {
                            currentLine.append(" ");
                        }
                        currentLineLength = textRenderer.width(currentLine.toString());
                    } else if (currentLineLength == 0 && wordLength > maxLineLength) {
                        // Word too long and can't fit on an empty line: split by characters
                        StringBuilder part = new StringBuilder();
                        for (char c : word.toCharArray()) {
                            part.append(c);
                            if (textRenderer.width(part.toString()) > maxLineLength) {
                                // Push the previous part (excluding this char) as a new line
                                rawLines.add(0, word.substring(part.length() - 1)); // remainder
                                word = part.substring(0, part.length() - 1);
                                break;
                            }
                        }
                        rawLines.add(0, word); // re-add the split word
                        break; // Restart processing with the split word
                    } else {
                        nextLine.append(word);
                        if (!MiapiConfig.getClientConfig().other.splitNewLineAlways) {
                            nextLine.append(" ");
                        }
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
