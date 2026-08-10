package smartin.miapi.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.editor.syntax.EditorInterface;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataPackContext {
    public String name;
    public String id;
    public String author;
    public String description;
    public boolean enabled;
    public String dataPath;
    public boolean watchFiles = true;
    public transient File directory;
    public transient boolean passedValidation = true;
    public transient Map<String, LiveDataPackManager.ValidationCache> validatedFiles = new HashMap<>();

    public void repair() {
        if (dataPath == null) {
            dataPath = "data";
        }
        if (validatedFiles == null) {
            validatedFiles = new HashMap<>();
        }
    }

    public void createDatapack(ZipOutputStream zos, File dataPath) {
        try {
            if (dataPath == null || !dataPath.exists() || !dataPath.isDirectory()) {
                throw new IllegalArgumentException("Data path must exist and be a directory: " + dataPath);
            }

            Path basePath = dataPath.toPath();

            Files.walk(basePath).forEach(path -> {
                try {
                    String relativePath = basePath.relativize(path).toString().replace("\\", "/");

                    if (Files.isDirectory(path)) {
                        // Store directory with trailing slash inside "data/"
                        if (!relativePath.isEmpty()) {
                            String dirPath = "data/" + relativePath + "/";
                            zos.putNextEntry(new ZipEntry(dirPath));
                            zos.closeEntry();
                        }
                        return;
                    }

                    // File path inside zip inside "data/"
                    String zipPath = "data/" + relativePath;

                    ZipEntry entry = new ZipEntry(zipPath);
                    zos.putNextEntry(entry);

                    try (InputStream is = Files.newInputStream(path)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }

                    zos.closeEntry();
                } catch (IOException e) {
                    EditorLogger.getLogger().warn("Could not write data ", e);
                }
            });
        } catch (IOException e) {
            EditorLogger.getLogger().warn("Could not write data ", e);
        }
    }


    @Nullable
    public LiveDataPackManager.ValidationCache getValidationCache(String relativePath, File file, boolean forced) {
        LiveDataPackManager.ValidationCache cache = validatedFiles.get(relativePath);
        if (cache == null || cache.lastModified() != file.lastModified() || !cache.blockLoad() && forced) {
            try {
                // Convert relative path to ResourceLocation
                // Remove .json extension and replace path separators with /
                String pathWithoutExt = relativePath.replace(".json", "").replace("\\", "/");
                pathWithoutExt = pathWithoutExt.replaceFirst("/", ":");
                ResourceLocation resourceLocation = Miapi.id(pathWithoutExt);

                // Get interfaces through event system
                List<EditorInterface> interfaces = new ArrayList<>();
                EditorEvents.EDITOR_INTERFACES.invoker().onGetInterfaces(
                        new EditorEvents.EditorInterfaceData(resourceLocation, file.getPath(), interfaces)
                );

                // Read and parse the file content
                String content = Files.readString(file.toPath());
                JsonElement json = JsonParser.parseString(content);
                boolean shouldLoad = true;

                if (json.isJsonObject()) {
                    if (json.getAsJsonObject().has("load_condition")) {
                        shouldLoad = ConditionManager.get(json.getAsJsonObject().get("load_condition")).isAllowed(new ConditionManager.ConditionContext() {
                            @Override
                            public ConditionManager.ConditionContext copy() {
                                return this;
                            }
                        });
                    }
                }
                if (shouldLoad) {
                    // Validate using all interfaces
                    boolean isValid = true;
                    List<EditorError> allErrors = new ArrayList<>();
                    for (EditorInterface iface : interfaces) {
                        List<EditorError> errors = iface.validateContent(json, content, 0);
                        allErrors.addAll(errors);
                        // File is invalid if there are any errors (not just warnings)
                        if (errors.stream().anyMatch(error -> error.severity() == EditorError.ErrorSeverity.ERROR)) {
                            isValid = false;
                            break;
                        }
                        if (errors.stream().anyMatch(error -> error.severity() == EditorError.ErrorSeverity.WARNING)) {
                            //isValid = false;
                            //break;
                        }
                    }

                    // Cache the result
                    long lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
                    cache = new LiveDataPackManager.ValidationCache(lastModified, shouldLoad, !(shouldLoad && isValid), allErrors);
                    if (cache.blockLoad()) {
                        EditorLogger.getLogger().warn("could not validate file " + file.toPath());
                    }
                    validatedFiles.put(relativePath, cache);
                } else {
                    long lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
                    cache = new LiveDataPackManager.ValidationCache(lastModified, shouldLoad, false, List.of());
                    validatedFiles.put(relativePath, cache);
                }

            } catch (Exception e) {
                // If any error occurs during validation, consider the file invalid
                long lastModified = 0;
                try {
                    lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
                } catch (IOException ex) {
                    EditorLogger.getLogger().warn("", ex);
                }
                cache = new LiveDataPackManager.ValidationCache(lastModified, false, false, List.of(new EditorError(0, "Critical load issue," + e.getMessage(), EditorError.ErrorSeverity.ERROR)));
                validatedFiles.put(relativePath, cache);
            }
        }
        boolean isValid = cache != null && cache.shouldLoad();
        if (!isValid) {
            EditorLogger.getLogger().warn("fail");
        }
        return cache;
    }

    public boolean isFileValid(String relativePath, File file) {
        LiveDataPackManager.ValidationCache cache = getValidationCache(relativePath, file, false);
        boolean isValid = cache != null && cache.shouldLoad();
        if (!isValid) {
            EditorLogger.getLogger().warn("fail");
        }
        return isValid;
    }

    private boolean validateFile(String relativePath, File file) {
        try {
            // Basic JSON validation
            String content = Files.readString(file.toPath());
            JsonParser.parseString(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
