package smartin.miapi.editor;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class LiveDataPackEditorManager implements MiapiEditor {
    private final LiveDataPackManager manager;
    private final LiveDataPackViewer viewer;

    public LiveDataPackEditorManager() {
        this.manager = LiveDataPackManager.getInstance();
        this.viewer = new LiveDataPackViewer(manager);
    }

    public static LiveDataPackEditorManager openUI;

    static {
        LiveDataPackManager.isEnabled = LiveDataPackEditorManager::isEnabled;
    }

    public static void openLivePackEditor() {
        if (openUI != null) {
            openUI.viewer.show.set(true);
            if (!editors.contains(openUI)) {
                editors.add(openUI);
            }
        } else {
            openUI = new LiveDataPackEditorManager();
            editors.add(openUI);
        }
    }

    public static boolean isEnabled() {
        if (openUI != null) {
            return openUI.viewer.show.get();
        } else {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        viewer.render(guiGraphics, deltaTracker);
    }
}
