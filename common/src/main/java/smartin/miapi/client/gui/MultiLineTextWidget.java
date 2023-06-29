package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class MultiLineTextWidget extends InteractAbleWidget {

    public Text rawText;
    public List<Text> lines = new ArrayList<>();
    private int longestLine;
    public int maxLineLength = -1;
    public TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    public int spacer = 1;
    public boolean hasTextShadow = true;
    public int textColor = ColorHelper.Argb.getArgb(255, 255, 255, 255);


    public MultiLineTextWidget(int x, int y, int width, int height, Text text) {
        super(x, y, width, height, Text.empty());
        maxLineLength = width;
        setText(text);
    }

    public void setText(Text text) {
        rawText = text;
        List<String> rawLines = Arrays.stream(text.getString().split("\n")).collect(Collectors.toList());
        lines.clear();

        while (!rawLines.isEmpty()) {
            String rawLine = rawLines.remove(0);
            if (maxLineLength > 0 && textRenderer.getWidth(rawLine) > maxLineLength) {
                List<String> words = new ArrayList<>(Arrays.stream(rawLine.split(" ")).toList());
                StringBuilder currentLine = new StringBuilder();
                currentLine.append(words.remove(0));
                currentLine.append(" ");
                StringBuilder nextLine = new StringBuilder();
                int currentLineLength = 0;

                boolean isLineExceeded = false;
                for (String word : words) {
                    int wordLength = textRenderer.getWidth(word);

                    if ((!isLineExceeded && currentLineLength + wordLength + 1 <= maxLineLength)) {
                        // Include the word in the current line if it's the first word or if the line has already exceeded the maximum length
                        currentLine.append(word).append(" ");
                        currentLineLength = textRenderer.getWidth(currentLine.toString());
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

            Text line = Text.translatable(rawLine);
            int size = textRenderer.getWidth(line);
            if (size > longestLine) {
                longestLine = size;
            }
            lines.add(line);
        }
        this.width = longestLine;
        this.height = lines.size() * textRenderer.fontHeight + Math.max(0, lines.size() - 1) * spacer;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int start = getY();
        for (Text line : lines) {
            context.drawText(textRenderer,line,getX(),start,textColor,hasTextShadow);
            start += textRenderer.fontHeight + spacer;
        }

    }
}
