package smartin.miapi.editor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;

public class JsonEditor {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ImString content;
    private final String title;
    private final JsonElement initialValue;

    public JsonEditor(String title, JsonElement initialValue) {
        this.title = title;
        this.initialValue = initialValue;
        this.content = new ImString(4096);
        this.content.set(GSON.toJson(initialValue));
    }

    public void render() {
        if (ImGui.collapsingHeader(title+"##"+ System.identityHashCode(this))) {
            if (ImGui.inputTextMultiline("##" + title, content, ImGuiInputTextFlags.None)) {
                try {
                    JsonParser.parseString(content.get());
                } catch (Exception e) {
                    ImGui.text("Invalid JSON!");
                }
            }

            if (ImGui.button("Format")) {
                try {
                    JsonElement element = JsonParser.parseString(content.get());
                    content.set(GSON.toJson(element));
                } catch (Exception e) {
                    ImGui.text("Invalid JSON!");
                }
            }
        }
    }

    public JsonElement getValue() {
        try {
            return JsonParser.parseString(content.get());
        } catch (Exception e) {
            return initialValue;
        }
    }
} 