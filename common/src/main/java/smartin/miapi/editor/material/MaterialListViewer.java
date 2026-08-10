package smartin.miapi.editor.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.EditorLogger;
import smartin.miapi.editor.JsonEditor;
import smartin.miapi.editor.MiapiEditor;
import smartin.miapi.material.codec.CodecMaterial;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MaterialListViewer implements MiapiEditor {
    private final File materialsDirectory;
    private final ImBoolean show = new ImBoolean(true);
    private final ImString newMaterialName = new ImString(128);

    public MaterialListViewer(File datapackDirectory, Runnable onChange) {
        this.materialsDirectory = datapackDirectory;
        if (!materialsDirectory.exists()) {
            materialsDirectory.mkdirs();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }

        ImGui.setNextWindowSize(500, 400, ImGuiCond.FirstUseEver);
        if (ImGui.begin("Materials", show)) {
            ImGui.inputText("New Material ID", newMaterialName);
            ImGui.sameLine();
            if (ImGui.button("Create")) {
                String name = newMaterialName.get().trim();
                if (!name.isEmpty()) {
                    try {
                        ResourceLocation id = Miapi.id(name);
                        File newFile = new File(materialsDirectory, id.getNamespace() + "/miapi/materials/" + id.getPath() + ".json");
                        if (!newFile.exists()) {
                            newFile.getParentFile().mkdirs();
                            newFile.createNewFile();

                            CodecMaterial newMaterial = CodecMaterial.CODEC
                                    .decode(JsonOpsBooleanPatched.INSTANCE, new JsonObject())
                                    .getOrThrow()
                                    .getFirst();

                            newMaterial.setID(id);
                            writeToFile(newMaterial, newFile);

                            // Instead of MaterialEditor, open JsonEditor
                            JsonEditor jsonEditor = new JsonEditor(
                                    Files.readString(newFile.toPath()), // content
                                    (newContent) -> writeJsonToFile(newFile, newContent), // onChange callback
                                    newFile.toPath(),
                                    Miapi.id(id.getNamespace() + ":" + id.getPath() + "/miapi/materials") // resource location
                            );

                            MiapiEditor.editors.add(jsonEditor);
                        }

                    } catch (Exception e) {
                        ImGui.textColored(1, 0, 0, 1, "Failed to create material: " + e.getMessage());
                    }
                }
            }

            ImGui.separator();
            if (materialsDirectory.exists()) {
                List<File> materialFiles = new ArrayList<>();
                scanAllMaterialFiles(materialsDirectory, materialFiles);

                materialFiles.sort(Comparator.comparing(File::getAbsolutePath));

                for (File file : materialFiles) {
                    String relativePath = materialsDirectory.toPath().relativize(file.toPath()).toString();

                    if (ImGui.selectable(relativePath)) {
                        try {
                            String pathWithoutExt = relativePath.replace(".json", "").replace("\\", "/");
                            pathWithoutExt = pathWithoutExt.replaceFirst("/", ":");
                            ResourceLocation resourceLocation = Miapi.id(pathWithoutExt);

                            JsonEditor jsonEditor = new JsonEditor(
                                    Files.readString(file.toPath()),
                                    (newContent) -> writeJsonToFile(file, newContent),
                                    file.toPath(),
                                    resourceLocation
                            );

                            MiapiEditor.editors.add(jsonEditor);
                        } catch (IOException e) {
                            e.printStackTrace();
                            ImGui.textColored(1, 0, 0, 1, "Error opening file " + relativePath);
                        }
                    }

                }

                if (materialFiles.isEmpty()) {
                    ImGui.text("No materials found.");
                }
            } else {
                ImGui.text("miapi/materials directory not found.");
            }


        }
        ImGui.end();
    }

    private JsonObject readJsonFromFile(Path directory, File file) throws IOException {
        Path fullPath = directory.resolve(file.toPath());
        try (Reader reader = Files.newBufferedReader(fullPath)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private void writeJsonToFile(File file, String jsonContent) {
        try (Writer writer = Files.newBufferedWriter(file.toPath())) {
            writer.write(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
            ImGui.textColored(1, 0, 0, 1, "Failed to save " + file.getName());
        }
    }


    private void scanAllMaterialFiles(File dataFolder, List<File> result) {
        File[] namespaces = dataFolder.listFiles(File::isDirectory);
        if (namespaces == null) return;

        for (File namespaceFolder : namespaces) {
            File materialRoot = new File(namespaceFolder, "miapi/materials");
            if (materialRoot.exists() && materialRoot.isDirectory()) {
                scanMaterialFiles(materialRoot, result);
            }
        }
    }

    private void scanMaterialFiles(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanMaterialFiles(file, result);
            } else if (file.getName().endsWith(".json")) {
                result.add(file);
            }
        }
    }


    public static CodecMaterial readFromFile(File dataDir, File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);
            DataResult<CodecMaterial> result = CodecMaterial.CODEC.parse(JsonOpsBooleanPatched.INSTANCE, json);
            CodecMaterial material = result.result().orElse(null);

            if (material != null) {
                ResourceLocation id = inferMaterialId(dataDir, file);
                material.setID(id);
            }

            return material;
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to read CodecMaterial from file: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    private static ResourceLocation inferMaterialId(File dataDir, File materialFile) throws IOException {
        String dataPath = dataDir.getCanonicalPath();
        String materialPath = materialFile.getCanonicalPath();

        if (!materialPath.startsWith(dataPath)) {
            throw new IOException("Material file is not under the data directory");
        }

        String relative = materialPath.substring(dataPath.length() + 1); // skip separator
        // Example: asd/miapi/materials/test/testing.json
        String[] parts = relative.split(File.separator.equals("\\") ? "\\\\" : File.separator, 2);
        if (parts.length < 2) {
            throw new IOException("Invalid material file structure: " + relative);
        }

        String namespace = parts[0];
        String insidePath = parts[1];

        // Remove prefix "miapi/materials/"
        String prefix = "miapi" + File.separator + "materials" + File.separator;
        if (!insidePath.startsWith(prefix)) {
            throw new IOException("Material not in miapi/materials: " + insidePath);
        }

        String materialIdPath = insidePath.substring(prefix.length()).replaceAll("\\.json$", "").replace(File.separatorChar, '/');

        return ResourceLocation.fromNamespaceAndPath(namespace, materialIdPath);
    }


    public static void writeToFile(CodecMaterial material, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            DataResult<JsonElement> encoded = CodecMaterial.CODEC.encodeStart(JsonOpsBooleanPatched.INSTANCE, material);
            JsonElement json = encoded.result().orElseThrow(() -> new IOException("Failed to encode CodecMaterial"));
            Miapi.gson.toJson(json, writer);
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to write CodecMaterial to file: {}", file.getAbsolutePath(), e);
        }
    }
}
