package smartin.miapi.editor;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FileSystemViewer implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final List<FileNode> rootNodes = new ArrayList<>();
    private JsonEditor jsonEditor;
    private final File rootDirectory;
    private final ImString newFolderName = new ImString(64);
    private final ImString newFileName = new ImString(64);
    private final ImBoolean showNewFolderPopup = new ImBoolean(false);
    private final ImBoolean showNewFilePopup = new ImBoolean(false);
    Consumer<File> changed;

    public FileSystemViewer(File rootDirectory, Consumer<File> changed) {
        this.rootDirectory = rootDirectory;
        updateFileList();
        this.changed = changed;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    private void updateFileList() {
        rootNodes.clear();
        File[] files = rootDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                rootNodes.add(new FileNode(file, file.getName(), (f, node) -> {
                    openJsonFile(f, node.relativePath);
                }));
            }
        }
        rootNodes.sort((a, b) -> {
            if (a.isDirectory && !b.isDirectory) return -1;
            if (!a.isDirectory && b.isDirectory) return 1;
            return a.name.compareTo(b.name);
        });
    }

    private void createNewFolder() {
        String folderName = newFolderName.get().trim();
        if (!folderName.isEmpty()) {
            File newFolder = new File(rootDirectory, folderName);
            if (!newFolder.exists()) {
                if (newFolder.mkdir()) {
                    updateFileList();
                    showNewFolderPopup.set(false);
                    newFolderName.clear();
                }
            }
        }
    }

    private void createNewFile() {
        String fileName = newFileName.get().trim();
        if (!fileName.isEmpty()) {
            if (!fileName.endsWith(".json")) {
                fileName += ".json";
            }
            File newFile = new File(rootDirectory, fileName);
            if (!newFile.exists()) {
                try {
                    Files.writeString(newFile.toPath(), "{}");
                    updateFileList();
                    showNewFilePopup.set(false);
                    newFileName.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }

        ImGui.setNextWindowSize(800, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin("File System Viewer##"+ System.identityHashCode(this), show)) {
            // Add buttons at the top
            /*
            if (ImGui.button("New Folder")) {
                showNewFolderPopup.set(true);
                newFolderName.clear();
            }
            ImGui.sameLine();
            if (ImGui.button("New File")) {
                showNewFilePopup.set(true);
                newFileName.clear();
            }
             */

            ImGui.separator();

            renderFileTree();
            ImGui.end();
        }

        // Render new folder popup
        if (showNewFolderPopup.get()) {
            ImGui.setNextWindowSize(300, 100, ImGuiCond.FirstUseEver);
            if (ImGui.begin("Create New Folder", showNewFolderPopup)) {
                ImGui.inputText("Folder Name", newFolderName);

                if (ImGui.button("Create")) {
                    createNewFolder();
                }
                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    showNewFolderPopup.set(false);
                    newFolderName.clear();
                }
                ImGui.end();
            }
        }

        // Render new file popup
        if (showNewFilePopup.get()) {
            ImGui.setNextWindowSize(300, 100, ImGuiCond.FirstUseEver);
            if (ImGui.begin("Create New File", showNewFilePopup)) {
                ImGui.inputText("File Name", newFileName);
                ImGui.text("Note: .json will be added automatically if not specified");

                if (ImGui.button("Create")) {
                    createNewFile();
                }
                ImGui.sameLine();
                if (ImGui.button("Cancel")) {
                    showNewFilePopup.set(false);
                    newFileName.clear();
                }
                ImGui.end();
            }
        }

        if (jsonEditor != null) {
            jsonEditor.render(guiGraphics, deltaTracker);
        }
    }

    private void renderFileTree() {
        for (FileNode node : rootNodes) {
            node.render();
        }
    }

    private void openJsonFile(File file, String relativePath) {
        openJsonFile(file, relativePath, changed);
    }

    public static void openJsonFile(File file, String relativePath, Consumer<File> onChange) {
        try {
            String content = Files.readString(file.toPath());
            String pathWithoutExt = relativePath.replace(".json", "").replace("\\", "/");
            pathWithoutExt = pathWithoutExt.replaceFirst("/", ":");
            ResourceLocation resourceLocation = Miapi.id(pathWithoutExt);

            JsonEditor jsonEditor = new JsonEditor(content, (newContent) -> {
                onChange.accept(file);
            }, file.toPath(), resourceLocation);

            if (file.getName().endsWith(".java")) {
                jsonEditor.setReadOnly(true);
            }

            MiapiEditor.editors.add(jsonEditor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class FileNode {
        private final File file;
        private final String name;
        private final boolean isDirectory;
        private final BiConsumer<File, FileNode> onFileClick;
        private final List<FileNode> children = new ArrayList<>();
        private boolean isExpanded = false;
        private String relativePath;

        public FileNode(File file, String relativePath, BiConsumer<File, FileNode> onFileClick) {
            this.file = file;
            this.name = file.getName();
            this.isDirectory = file.isDirectory();
            this.onFileClick = onFileClick;
            this.relativePath = relativePath;
            if (isDirectory) {
                loadChildren();
            }
        }

        private void loadChildren() {
            File[] files = file.listFiles();
            if (files != null) {
                for (File childFile : files) {
                    children.add(new FileNode(childFile, relativePath + "/" + childFile.getName(), onFileClick));
                }
                children.sort((a, b) -> {
                    if (a.isDirectory && !b.isDirectory) return -1;
                    if (!a.isDirectory && b.isDirectory) return 1;
                    return a.name.compareTo(b.name);
                });
            }
        }

        public void render() {
            if (isDirectory) {
                if (ImGui.treeNode(name + "/")) {
                    isExpanded = true;
                    for (FileNode child : children) {
                        child.render();
                    }
                    ImGui.treePop();
                } else {
                    isExpanded = false;
                }
            } else {
                if (ImGui.treeNodeEx(name)) {
                    if (ImGui.isItemClicked() && name.endsWith(".json")) {
                        onFileClick.accept(file, this);
                    }
                    ImGui.treePop();
                }
            }
        }
    }
} 