package smartin.miapi.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class JsonEditor implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final ImString content = new ImString(4096);
    private final Consumer<String> onChange;
    private boolean isValidJson = true;
    private String errorMessage = "";

    public JsonEditor(String initialContent, Consumer<String> onChange) {
        this.content.set(initialContent);
        this.onChange = onChange;
        validateJson();
    }

    private void validateJson() {
        try {
            JsonParser.parseString(content.get());
            isValidJson = true;
            errorMessage = "";
        } catch (Exception e) {
            isValidJson = false;
            errorMessage = e.getMessage();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) return;

        ImGui.setNextWindowSize(800, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin("JSON Editor", show)) {
            float windowWidth = ImGui.getWindowWidth();
            float windowHeight = ImGui.getWindowHeight();
            float contentHeight = windowHeight - 20; // Leave space for buttons and error message

            if (ImGui.inputTextMultiline("##content", content,windowWidth - 16, contentHeight, ImGuiInputTextFlags.None)) {
                validateJson();
            }

            if (!isValidJson) {
                //ImGui.pushStyleColor(ImGui.Col.Text, 1.0f, 0.0f, 0.0f, 1.0f);
                ImGui.text("Invalid JSON: " + errorMessage);
                //ImGui.popStyleColor();
            }

            ImGui.separator();

            if (ImGui.button("Save")) {
                if (isValidJson) {
                    onChange.accept(content.get());
                }
            }
            ImGui.sameLine();
            if (ImGui.button("Format")) {
                try {
                    JsonElement json = JsonParser.parseString(content.get());
                    content.set(json.toString());
                    validateJson();
                } catch (Exception e) {
                    isValidJson = false;
                    errorMessage = e.getMessage();
                }
            }

            ImGui.end();
        }
    }
}