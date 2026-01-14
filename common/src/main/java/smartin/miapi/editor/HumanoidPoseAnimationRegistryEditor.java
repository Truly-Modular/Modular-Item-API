package smartin.miapi.editor;

import com.google.gson.JsonObject;
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
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.Map;

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
        ImGui.begin(BASE_TITLE+"##"+ System.identityHashCode(this), show);

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
                break; // to avoid concurrent modification
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
        String data = HumanoidPoseAnimation.codec
                .encodeStart(JsonOpsBooleanPatched.INSTANCE, animation)
                .getOrThrow()
                .toString();

        // Copy to clipboard using Minecraft system clipboard utility
        Minecraft.getInstance().keyboardHandler.setClipboard(data);

        // Optional: feedback
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Exported animation '" + key + "' to clipboard."));
    }

}
