package smartin.miapi.editor.pose;

import com.redpxnda.nucleus.pose.client.HumanoidPoseAnimation;
import imgui.ImGui;

import java.util.EnumMap;
import java.util.function.Consumer;

public class FrameEditor {
    private final HumanoidPoseAnimation.Frame frame;
    private final Consumer<HumanoidPoseAnimation.Frame> onChange;
    private final Runnable onDelete;
    private final EnumMap<PartType, PartStateEditor> editorCache = new EnumMap<>(PartType.class);

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
                    renderPart(type.name().replaceAll("([A-Z])", " $1").trim(), part, p -> setPart(type, p), type);

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

    private void renderPart(String label, HumanoidPoseAnimation.PartState partState, Consumer<HumanoidPoseAnimation.PartState> setter, PartType type) {
        if (ImGui.treeNode(label + "##" + System.identityHashCode(this))) {

            // Check for cached editor
            PartStateEditor editor = editorCache.get(type);

            // Update cache if partState changed (added, replaced, or recreated)
            if (partState == null) {
                editorCache.remove(type);
            } else if (editor == null || editor.getMultiplier() != partState) {
                editor = new PartStateEditor(partState, updated -> {
                    setter.accept(updated);
                    triggerChange();
                });
                editorCache.put(type, editor);
            }

            if (editor != null) {
                editor.render();
            }

            // Delete button
            if (ImGui.button("Delete " + label)) {
                setter.accept(null); // remove part
                editorCache.remove(type); // remove editor
                triggerChange();
                ImGui.treePop();
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
