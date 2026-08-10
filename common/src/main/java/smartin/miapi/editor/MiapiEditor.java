package smartin.miapi.editor;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public interface MiapiEditor extends AutoCloseable {
    // Use CopyOnWriteArrayList for thread-safe concurrent access
    public static final List<MiapiEditor> editors = new CopyOnWriteArrayList<>();

    void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker);

    default void close() {
        // Implement close() to remove self from editors list
        editors.remove(this);
    }

    static void renderAll(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        // CopyOnWriteArrayList is already thread-safe, no need to copy
        editors.forEach(miapiEditor -> miapiEditor.render(guiGraphics, deltaTracker));
    }

}
