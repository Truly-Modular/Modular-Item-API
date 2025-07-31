package smartin.miapi.editor.renderers;

import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;

import java.util.function.Consumer;

public class FrameEditor {
    private final HumanoidPoseAnimation.Frame frame;
    private final Consumer<HumanoidPoseAnimation.Frame> onChange;
    private final Runnable onDelete;

    public FrameEditor(HumanoidPoseAnimation.Frame frame, Consumer<HumanoidPoseAnimation.Frame> onChange, Runnable onDelete) {
        this.frame = frame;
        this.onChange = onChange;
        this.onDelete = onDelete;
    }

    public void render() {
        if (ImGui.collapsingHeader("Frame##" + System.identityHashCode(this))) {

            if (ImGui.button("Delete Frame##deleteButton" + System.identityHashCode(this))) {
                onDelete.run();
                return; // Skip rendering if frame is deleted
            }

            // 1. Render non-null parts
            for (PartType type : PartType.values()) {
                HumanoidPoseAnimation.PartState part = getPart(type);
                if (part != null) {
                    renderPart(type.name().replaceAll("([A-Z])", " $1").trim(), part, p -> setPart(type, p));
                }
            }

            // 2. Add missing parts
            if (ImGui.beginCombo("Add Part##partAdder" + System.identityHashCode(this), "Select...")) {
                for (PartType type : PartType.values()) {
                    if (getPart(type) == null) {
                        if (ImGui.selectable(type.name())) {
                            setPart(type, createDefaultPartState());
                            triggerChange();
                        }
                    }
                }
                ImGui.endCombo();
            }

            // End time editor
            ImGui.text("End Time:");
            float[] endTime = new float[]{frame.endTime};
            if (ImGui.dragFloat("##endTime" + System.identityHashCode(this), endTime, 0.01f, 0.0f, Float.MAX_VALUE)) {
                frame.endTime = endTime[0];
                triggerChange();
            }
        }

    }

    private HumanoidPoseAnimation.PartState getPart(PartType type) {
        switch (type) {
            case Head:
                return frame.head;
            case Body:
                return frame.body;
            case FpUsedArm:
                return frame.fpUsedArm;
            case FpUnusedArm:
                return frame.fpUnusedArm;
            case FpRightArm:
                return frame.fpRightArm;
            case FpLeftArm:
                return frame.fpLeftArm;
            case UsedArm:
                return frame.usedArm;
            case UnusedArm:
                return frame.unusedArm;
            case RightArm:
                return frame.rightArm;
            case LeftArm:
                return frame.leftArm;
            case UsedItem:
                return frame.usedItem;
            case UnusedItem:
                return frame.unusedItem;
            case RightItem:
                return frame.rightItem;
            case LeftItem:
                return frame.leftItem;
            case RightLeg:
                return frame.rightLeg;
            case LeftLeg:
                return frame.leftLeg;
            default:
                return null;
        }
    }

    private void setPart(PartType type, HumanoidPoseAnimation.PartState state) {
        switch (type) {
            case Head:
                frame.head = state;
                break;
            case Body:
                frame.body = state;
                break;
            case FpUsedArm:
                frame.fpUsedArm = state;
                break;
            case FpUnusedArm:
                frame.fpUnusedArm = state;
                break;
            case FpRightArm:
                frame.fpRightArm = state;
                break;
            case FpLeftArm:
                frame.fpLeftArm = state;
                break;
            case UsedArm:
                frame.usedArm = state;
                break;
            case UnusedArm:
                frame.unusedArm = state;
                break;
            case RightArm:
                frame.rightArm = state;
                break;
            case LeftArm:
                frame.leftArm = state;
                break;
            case UsedItem:
                frame.usedItem = state;
                break;
            case UnusedItem:
                frame.unusedItem = state;
                break;
            case RightItem:
                frame.rightItem = state;
                break;
            case LeftItem:
                frame.leftItem = state;
                break;
            case RightLeg:
                frame.rightLeg = state;
                break;
            case LeftLeg:
                frame.leftLeg = state;
                break;
        }
    }

    private void renderPart(String label, HumanoidPoseAnimation.PartState partState, Consumer<HumanoidPoseAnimation.PartState> setter) {
        if (ImGui.treeNode(label+"##"+System.identityHashCode(this))) {
            new PartStateEditor(partState, updated -> {
                setter.accept(updated);
                triggerChange();
            }).render();

            // Add Delete Button for the Part
            if (ImGui.button("Delete " + label)) {
                setter.accept(null); // Set the part to null
                triggerChange();
                ImGui.treePop(); // Close the tree early since part is now deleted
                return;
            }

            ImGui.treePop();
        }
    }

    private enum PartType {
        Head, Body,
        FpUsedArm, FpUnusedArm, FpRightArm, FpLeftArm,
        UsedArm, UnusedArm, RightArm, LeftArm,
        UsedItem, UnusedItem, RightItem, LeftItem,
        RightLeg, LeftLeg
    }

    private HumanoidPoseAnimation.PartState createDefaultPartState() {
        HumanoidPoseAnimation.PartState state = new HumanoidPoseAnimation.PartState();
        return state;
    }


    private void triggerChange() {
        onChange.accept(frame);
    }

    public HumanoidPoseAnimation.Frame getFrame() {
        return frame;
    }
}
