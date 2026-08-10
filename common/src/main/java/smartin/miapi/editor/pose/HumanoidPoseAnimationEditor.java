package smartin.miapi.editor.pose;

import com.google.gson.JsonPrimitive;
import com.redpxnda.nucleus.math.InterpolateMode;
import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.editor.MiapiEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HumanoidPoseAnimationEditor implements MiapiEditor {
    private final HumanoidPoseAnimation animation;
    private final ResourceLocation id;
    private final Consumer<HumanoidPoseAnimation> onChange;
    private final ImBoolean show = new ImBoolean(true);
    private final List<FrameEditor> frameEditors = new ArrayList<>();

    public HumanoidPoseAnimationEditor(ResourceLocation id, HumanoidPoseAnimation animation, Consumer<HumanoidPoseAnimation> onChange) {
        this.id = id;
        this.animation = animation;
        this.onChange = onChange;

        rebuildEditors();
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }
        ImGui.begin("Animation Editor: " + id.toString() + "##" + System.identityHashCode(this), show);

        ImGui.text("ID: " + id.toString());

        ImGui.separator();
        ImGui.text("Frames:");

        for (int i = 0; i < frameEditors.size(); i++) {
            frameEditors.get(i).render();
        }

        if (ImGui.button("Add Frame")) {
            HumanoidPoseAnimation.Frame newFrame = new HumanoidPoseAnimation.Frame();
            newFrame.interpolate = InterpolateMode.interpolateModes.get("easeInOut").createFrom(new JsonPrimitive("easeInOut"));
            animation.frames.add(newFrame);
            if (animation.frames.isEmpty()) {
                newFrame.endTime = 1;
            } else {
                newFrame.endTime = animation.frames.getLast().endTime + 1;
            }
            rebuildEditors();
            triggerChange();
        }

        ImGui.separator();
        ImInt loops = new ImInt(animation.loops);
        if (ImGui.inputInt("Loops", loops)) {
            animation.loops = loops.get();
            triggerChange();
        }

        ImGui.end();
    }

    private void triggerChange() {
        if (!animation.frames.isEmpty()) {
            animation.length = animation.frames.getLast().endTime;
        } else {
            animation.length = 0;
        }
        onChange.accept(animation);
    }

    @Override
    public void close() {
        MiapiEditor.editors.remove(this);
    }

    public ResourceLocation getId() {
        return id;
    }

    private void rebuildEditors() {
        frameEditors.clear();
        for (int i = 0; i < animation.frames.size(); i++) {
            HumanoidPoseAnimation.Frame frame = animation.frames.get(i);
            int index = i; // must be final or effectively final for lambda
            frameEditors.add(new FrameEditor(frame, f -> triggerChange(), () -> {
                animation.frames.remove(index);
                rebuildEditors();
                triggerChange();
            }));
        }
    }
}
