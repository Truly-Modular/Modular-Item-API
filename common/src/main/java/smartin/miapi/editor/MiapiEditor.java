package smartin.miapi.editor;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public interface MiapiEditor {
    List<MiapiEditor> editors = new ArrayList<>();

    void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
}
