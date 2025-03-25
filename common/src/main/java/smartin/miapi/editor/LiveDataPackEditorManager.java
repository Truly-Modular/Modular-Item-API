package smartin.miapi.editor;

import com.redpxnda.nucleus.editor.core.ClientLoader;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import smartin.miapi.modules.cache.CacheCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LiveDataPackEditorManager implements MiapiEditor {
    private final LiveDataPackManager manager;
    private FileSystemViewer fileSystemViewer;
    private final ImBoolean show = new ImBoolean(true);
    private final ImBoolean showCreateWindow = new ImBoolean(false);
    private final ImString newPackName = new ImString(64);
    private final ImString newPackId = new ImString(64);
    private final ImString newPackAuthor = new ImString(64);
    private final ImString newPackDescription = new ImString(256);
    private final ImBoolean newPackEnabled = new ImBoolean(true);
    private LiveDataPackManager.DataPackContext editingContext;
    private final ImString editName = new ImString(64);
    private final ImString editId = new ImString(64);
    private final ImString editAuthor = new ImString(64);
    private final ImString editDescription = new ImString(256);
    private final ImString editDataPath = new ImString(64);
    private final ImBoolean editWatchFiles = new ImBoolean(true);

    static {
        ClientLoader.RENDER.add((guiGraphics, deltaTracker) -> new ArrayList<>(editors).forEach(miapiEditor -> miapiEditor.render(guiGraphics, deltaTracker)));
    }

    public LiveDataPackEditorManager() {
        this.manager = LiveDataPackManager.getInstance();
    }

    private void clearNewPackFields() {
        newPackName.clear();
        newPackId.clear();
        newPackAuthor.clear();
        newPackDescription.clear();
        newPackEnabled.set(true);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) return;

        ImGui.setNextWindowSize(400, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin("DataPack Manager", show)) {
            // Top button row
            if (ImGui.button("Create New Pack")) {
                showCreateWindow.set(true);
                clearNewPackFields();
            }
            ImGui.sameLine();
            if (ImGui.button("Refresh")) {
                manager.scanForDataPacks();
            }
            ImGui.sameLine();
            if (ImGui.button("Reload")) {
                reload();
            }

            ImGui.separator();

            // List all packs
            List<LiveDataPackManager.DataPackContext> packContexts = manager.getLoadedPacks();
            for (LiveDataPackManager.DataPackContext pack : packContexts) {
                if (ImGui.treeNode(pack.name + " (" + pack.id + ")")) {
                    ImGui.text("Directory: " + pack.directory.getName());

                    if (editingContext == pack) {
                        // Edit mode
                        if (ImGui.inputText("Name", editName)) {
                            pack.name = editName.get();
                        }
                        if (ImGui.inputText("ID", editId)) {
                            pack.id = editId.get().toLowerCase();
                        }
                        if (ImGui.inputText("Author", editAuthor)) {
                            pack.author = editAuthor.get();
                        }
                        if (ImGui.inputTextMultiline("Description", editDescription)) {
                            pack.description = editDescription.get();
                        }
                        if (ImGui.inputText("Data Path", editDataPath)) {
                            pack.dataPath = editDataPath.get();
                        }
                        if (ImGui.checkbox("Watch Files", editWatchFiles)) {
                            pack.watchFiles = editWatchFiles.get();
                            if (pack.watchFiles) {
                                manager.watchDataPack(pack);
                            } else {
                                manager.unwatchDataPack(pack);
                            }
                        }
                        if (ImGui.checkbox("Enabled", pack.enabled)) {
                            pack.enabled = !pack.enabled;
                        }
                        if (ImGui.button("Save")) {
                            manager.saveContext(pack);
                            editingContext = null;
                        }
                        ImGui.sameLine();
                        if (ImGui.button("Cancel")) {
                            editingContext = null;
                        }
                    } else {
                        // View mode
                        ImGui.text("ID: " + pack.id);
                        ImGui.text("Author: " + pack.author);
                        ImGui.text("Description: " + pack.description);
                        ImGui.text("Data Path: " + pack.dataPath);
                        ImGui.text("Watch Files: " + (pack.watchFiles ? "Yes" : "No"));
                        ImGui.text("Enabled: " + (pack.enabled ? "Yes" : "No"));
                        if (ImGui.button("Edit")) {
                            editingContext = pack;
                            editName.set(pack.name);
                            editId.set(pack.id);
                            editAuthor.set(pack.author);
                            editDescription.set(pack.description);
                            editDataPath.set(pack.dataPath);
                            editWatchFiles.set(pack.watchFiles);
                        }
                    }

                    ImGui.sameLine();
                    if (ImGui.button("Open Files")) {
                        if (fileSystemViewer != null) {
                            ClientLoader.RENDER.remove(fileSystemViewer);
                        }
                        File dataDir = new File(pack.directory, pack.dataPath);
                        if (!dataDir.exists()) {
                            dataDir.mkdirs();
                        }
                        fileSystemViewer = new FileSystemViewer(dataDir, (file) -> {
                            reload();
                        });
                        MiapiEditor.editors.add(fileSystemViewer);
                    }

                    ImGui.sameLine();
                    if (ImGui.button("Delete")) {
                        manager.deletePack(pack);
                        if (fileSystemViewer != null && Objects.equals(fileSystemViewer.getRootDirectory(), pack.directory)) {
                            ClientLoader.RENDER.remove(fileSystemViewer);
                            fileSystemViewer = null;
                        }
                    }

                    ImGui.treePop();
                }
            }

            ImGui.end();
        }

        // Render create window
        if (showCreateWindow.get()) {
            ImGui.setNextWindowSize(400, 300, ImGuiCond.FirstUseEver);
            if (ImGui.begin("Create New DataPack", showCreateWindow)) {
                ImGui.inputText("Pack Name", newPackName);
                ImGui.inputText("Pack ID (lowercase)", newPackId);
                if (!newPackId.get().toLowerCase().equals(newPackId.get())) {
                    newPackId.set(newPackId.get().toLowerCase());
                }
                ImGui.inputText("Author", newPackAuthor);
                ImGui.inputTextMultiline("Description", newPackDescription);
                ImGui.checkbox("Enabled", newPackEnabled);

                boolean canCreate = !newPackName.get().trim().isEmpty() &&
                                    !newPackId.get().trim().isEmpty() &&
                                    !newPackAuthor.get().trim().isEmpty();

                if (!canCreate) {
                    ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Please fill in all required fields");
                }

                if (ImGui.button("Create") && canCreate) {
                    manager.createNewPack(
                            newPackName.get().trim(),
                            newPackId.get().trim(),
                            newPackAuthor.get().trim(),
                            newPackDescription.get().trim(),
                            newPackEnabled.get()
                    );
                    showCreateWindow.set(false);
                    clearNewPackFields();
                }
                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    showCreateWindow.set(false);
                    clearNewPackFields();
                }
            }
            ImGui.end();
        }
    }

    public void reload(){
        CacheCommands.triggerServerReload();
    }
}
