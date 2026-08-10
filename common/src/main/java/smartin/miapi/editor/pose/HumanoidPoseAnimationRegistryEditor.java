package smartin.miapi.editor.pose;

import com.google.gson.*;
import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import com.redpxnda.nucleus.pose.client.PoseAnimationResourceListener;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.MiapiEditor;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HumanoidPoseAnimationRegistryEditor implements MiapiEditor {
    private static final String BASE_TITLE = "Humanoid Pose Animation Registry";
    private final ImBoolean show = new ImBoolean(true);

    public HumanoidPoseAnimationRegistryEditor() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }
        ImGui.begin(BASE_TITLE + "##" + System.identityHashCode(this), show);

        ImGui.text("Registered Animations:");
        ImGui.separator();

        for (Map.Entry<String, HumanoidPoseAnimation> entry : PoseAnimationResourceListener.animations.entrySet()) {
            String key = entry.getKey();
            HumanoidPoseAnimation animation = entry.getValue();

            ImGui.pushID(key); // to isolate buttons for each row

            ImGui.text(key);
            ImGui.sameLine();
            if (ImGui.button("Edit")) {
                ResourceLocation id = Miapi.id(key);
                var editor = new HumanoidPoseAnimationEditor(id, animation, updated -> {
                    // Update registry entry
                    PoseAnimationResourceListener.animations.put(key, updated);
                });
                MiapiEditor.editors.add(editor);
            }

            ImGui.sameLine();
            if (ImGui.button("Export")) {
                exportAnimation(key, animation); // <-- implement this
            }

            ImGui.sameLine();
            if (ImGui.button("Remove")) {
                PoseAnimationResourceListener.animations.remove(key);
                // Removed animation from registry
            }

            ImGui.popID();
        }

        ImGui.separator();
        if (ImGui.button("Add New Animation")) {
            createExample(); // <-- implement this
        }

        ImGui.end();
    }

    @Override
    public void close() {
        MiapiEditor.editors.remove(this);
    }

    private void createExample() {
        // Decode an empty/default animation
        HumanoidPoseAnimation animation = HumanoidPoseAnimation.codec
                .decode(JsonOpsBooleanPatched.INSTANCE, new JsonObject())
                .getOrThrow()
                .getFirst();

        // Generate a unique name
        String baseName = "new_animation";
        String name = baseName;
        int counter = 1;
        while (PoseAnimationResourceListener.animations.containsKey(name)) {
            name = baseName + "_" + counter++;
        }
        String finalName = name;

        // Add to registry
        PoseAnimationResourceListener.animations.put(name, animation);

        // Open editor window
        ResourceLocation id = Miapi.id("custom", name);
        var editor = new HumanoidPoseAnimationEditor(id, animation, updated -> {
            PoseAnimationResourceListener.animations.put(finalName, updated);
        });
        MiapiEditor.editors.add(editor);
    }


    private void exportAnimation(String key, HumanoidPoseAnimation animation) {
        JsonElement element = HumanoidPoseAnimation.codec
                .encodeStart(JsonOpsBooleanPatched.INSTANCE, animation)
                .getOrThrow();
        if (element instanceof JsonObject jsonObject) {
            jsonObject.addProperty("name", key);
        }

        String data = formatJson(element);

        // Copy to clipboard using Minecraft system clipboard utility
        Minecraft.getInstance().keyboardHandler.setClipboard(data);

        // Optional: feedback
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Exported animation '" + key + "' to clipboard."));
    }

    private static String formatJson(JsonElement element) {
        element = removeNulls(element);

        String pretty = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(element);

        return inlineNumberArrays(pretty);
    }

    private static JsonElement removeNulls(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return JsonNull.INSTANCE;
        }

        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();

            for (var entry : element.getAsJsonObject().entrySet()) {
                JsonElement cleaned = removeNulls(entry.getValue());
                if (!cleaned.isJsonNull()) {
                    result.add(entry.getKey(), cleaned);
                }
            }

            return result;
        }

        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();

            for (JsonElement child : element.getAsJsonArray()) {
                JsonElement cleaned = removeNulls(child);
                if (!cleaned.isJsonNull()) {
                    result.add(cleaned);
                }
            }

            return result;
        }

        return element;
    }

    private static final Pattern NUMBER_ARRAY =
            Pattern.compile("\\[(?:\\s*-?(?:\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)\\s*,?\\s*)+]");

    private static String inlineNumberArrays(String json) {
        Matcher matcher = NUMBER_ARRAY.matcher(json);
        StringBuffer out = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group()
                    .replaceAll("\\s+", "")
                    .replace(",", ", ");

            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(out);
        return out.toString();
    }

}
