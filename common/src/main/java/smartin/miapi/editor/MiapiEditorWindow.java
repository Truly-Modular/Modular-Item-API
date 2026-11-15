package smartin.miapi.editor;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public abstract class MiapiEditorWindow implements MiapiEditor {
    protected String windowId;
    public final ImBoolean show = new ImBoolean(true);
    public int xInitSize = 800;
    public int yInitSize = 600;

    protected MiapiEditorWindow(String windowId) {
        this.windowId = windowId;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ImGui.setNextWindowSize(xInitSize, yInitSize, ImGuiCond.FirstUseEver);
        if (ImGui.begin("File System Viewer##" + System.identityHashCode(this), show)) {
            renderWindow(guiGraphics, deltaTracker);
            ImGui.end();
        }
        if (!show.get()) {
            close();
        }
    }

    abstract void renderWindow(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
}
