package smartin.miapi.editor;

import com.google.gson.*;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class LiveDataPackManager implements AutoCloseable {
    private static final String RUNTIME_FOLDER = "miapi_runtime_datapacks";
    private static final String CONTEXT_FILE = "miapi-editor-context.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiveDataPackManager INSTANCE;

    private final List<DataPackContext> loadedPacks = new ArrayList<>();
    private final File runtimeFolder;
    private WatchService watchService;
    private final Map<WatchKey, DataPackContext> watchKeys = new ConcurrentHashMap<>();
    public List<MiapiEditor> openedEditors = new ArrayList<>();
    boolean isValidating = false;
    public static Supplier<Boolean> isEnabled = () -> false;

    public static void setup() {
        MiapiEvents.PLAYER_TICK_END.register(player -> {
            if (Platform.getEnv() == EnvType.CLIENT && Miapi.server != null && INSTANCE != null) {
                if (INSTANCE.checkAndValidateDatapacks(false)) {
                    CacheCommands.triggerServerReload();
                }
            }
            return EventResult.pass();
        });
        MiapiEvents.ADJUST_RAW_DATA.register(event -> {
            if (Platform.getEnv() == EnvType.CLIENT && Miapi.server != null && isEnabled.get()) {
                getInstance().processDataPacks(event);
            }
            return EventResult.pass();
        });
    }


    public static LiveDataPackManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LiveDataPackManager();
        }
        return INSTANCE;
    }

    private LiveDataPackManager() {
        this.runtimeFolder = getRuntimeFolder();
        if (!runtimeFolder.exists()) {
            runtimeFolder.mkdirs();
        }
        setupFileWatcher();
        scanForDataPacks();
    }

    private File getRuntimeFolder() {
        return Platform.getGameFolder().resolve(RUNTIME_FOLDER).toFile();
    }

    private void setupFileWatcher() {
        try {
            watchService = runtimeFolder.toPath().getFileSystem().newWatchService();
            EditorLogger.getLogger().debug("File watcher setup successful");
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to setup file watcher: {}", e.getMessage());
            // Continue without file watching rather than crashing
        }
    }

    public void watchDataPack(DataPackContext context) {
        watchDataPack(context, new File(context.directory, context.dataPath));
    }

    public void watchDataPack(DataPackContext context, File dataDir) {
        if (!context.watchFiles) return;

        try {
            if (dataDir.exists() && dataDir.isDirectory()) {
                WatchKey key = dataDir.toPath().register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                watchKeys.put(key, context);
                if (dataDir.isDirectory()) {
                    File[] subDirs = dataDir.listFiles(File::isDirectory);
                    if (subDirs != null) {
                        for (File subDir : subDirs) {
                            if (subDir != null) {
                                watchDataPack(context, subDir);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to watch data pack directory: {}", e.getMessage());
        }
    }

    public void unwatchDataPack(DataPackContext context) {
        if (context == null) {
            EditorLogger.getLogger().warn("Cannot unwatch null context");
            return;
        }
        watchKeys.entrySet().removeIf(entry -> {
            if (entry.getValue() == context) {
                entry.getKey().cancel();
                return true;
            }
            return false;
        });
    }

    private boolean checkFileChanges() {
        if (watchService == null) return false;
        boolean changeOccured = false;
        try {
            WatchKey key = watchService.poll();
            if (key != null) {
                DataPackContext context = watchKeys.get(key);
                if (context != null && context.watchFiles) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.toString().endsWith(".json")) {
                            // Validate all files when any JSON file changes
                            //context.validateAllFiles();
                            changeOccured = true;
                        }
                        // Non-JSON file changes are ignored
                    }
                    key.reset();
                }
            }
        } catch (Exception e) {
            EditorLogger.getLogger().error("Error checking file changes: {}", e.getMessage());
        }
        return changeOccured;
    }

    public void scanForDataPacks() {
        // Unwatch all existing packs
        loadedPacks.forEach(this::unwatchDataPack);
        watchKeys.clear();

        loadedPacks.clear();
        File[] directories = runtimeFolder.listFiles(File::isDirectory);

        if (directories != null) {
            for (File dir : directories) {
                if (dir != null) {
                    DataPackContext context = loadOrCreateContext(dir);
                    if (context != null) {
                        loadedPacks.add(context);
                        watchDataPack(context);
                    }
                }
            }
        }
    }

    public DataPackContext createNewPack(String name, String id, String author, String description, boolean enabled) {
        if (name.isEmpty() || id.isEmpty()) {
            EditorLogger.getLogger().warn("Cannot create pack with empty name or id");
            return null;
        }
        
        id = id.toLowerCase();
        File newDir = new File(runtimeFolder, id);
        if (!newDir.exists()) {
            if (!newDir.mkdirs()) {
                EditorLogger.getLogger().error("Failed to create directory: {}", id);
                return null;
            }
            DataPackContext context = createDefaultContext(newDir);
            context.name = name;
            context.id = id;
            context.author = author;
            context.description = description;
            context.enabled = enabled;
            context.dataPath = "data";
            if (!saveContext(context)) {
                EditorLogger.getLogger().error("Failed to save new pack context");
                return null;
            }
            loadedPacks.add(context);
            watchDataPack(context);
            return context;
        } else {
            EditorLogger.getLogger().warn("Directory already exists: {}", id);
            return null;
        }
    }

    public void deletePack(DataPackContext context) {
        if (context == null) {
            EditorLogger.getLogger().warn("Cannot delete null context");
            return;
        }
        try {
            unwatchDataPack(context);
            if (!deleteDirectory(context.directory)) {
                EditorLogger.getLogger().error("Failed to delete directory: {}", context.directory.getAbsolutePath());
            }
            loadedPacks.remove(context);
            EditorLogger.getLogger().info("Deleted data pack: {}", context.id);
        } catch (Exception e) {
            EditorLogger.getLogger().error("Error deleting data pack: {}", e.getMessage());
        }
    }

    private boolean deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return true;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    try {
                        // Make file writable before deletion
                        if (file.setWritable(true)) {
                            Files.delete(file.toPath());
                        } else {
                            EditorLogger.getLogger().error("Cannot make file writable: {}", file.getAbsolutePath());
                            return false;
                        }
                    } catch (IOException e) {
                        EditorLogger.getLogger().error("Failed to delete file: {} - {}", file.getName(), e.getMessage());
                        return false;
                    }
                }
            }
        }
        
        try {
            Files.delete(directory.toPath());
            return true;
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to delete directory: {} - {}", directory.getName(), e.getMessage());
            return false;
        }
    }

    private DataPackContext loadOrCreateContext(File directory) {
        File contextFile = new File(directory, CONTEXT_FILE);
        DataPackContext context;

        if (contextFile.exists()) {
            try {
                String json = Files.readString(contextFile.toPath());
                context = GSON.fromJson(json, DataPackContext.class);
                if (context != null) {
                    context.directory = directory;
                    context.repair();
                    EditorLogger.getLogger().debug("Loaded context from file: {}", context.id);
                } else {
                    EditorLogger.getLogger().warn("Failed to deserialize context from file: {}", contextFile.getAbsolutePath());
                    context = createDefaultContext(directory);
                }
            } catch (IOException e) {
                EditorLogger.getLogger().error("Failed to read context file: {} - {}", contextFile.getName(), e.getMessage());
                context = createDefaultContext(directory);
            }
        } else {
            EditorLogger.getLogger().debug("Creating new context for directory: {}", directory.getName());
            context = createDefaultContext(directory);
        }

        if (!saveContext(context)) {
            EditorLogger.getLogger().error("Failed to save context after loading: {}", context.id);
        }
        return context;
    }

    private DataPackContext createDefaultContext(File directory) {
        DataPackContext context = new DataPackContext();
        context.name = directory.getName();
        context.id = directory.getName().toLowerCase();
        context.author = "Unknown";
        context.description = "A MIAPI runtime datapack";
        context.enabled = true;
        context.dataPath = "data";
        context.directory = directory;
        return context;
    }

    public boolean saveContext(DataPackContext context) {
        if (context == null) {
            EditorLogger.getLogger().warn("Cannot save null context");
            return false;
        }
        try {
            File contextFile = new File(context.directory, CONTEXT_FILE);
            String json = GSON.toJson(context);
            Files.writeString(contextFile.toPath(), json);
            EditorLogger.getLogger().debug("Saved context for pack: {}", context.id);
            return true;
        } catch (IOException e) {
            EditorLogger.getLogger().error("Failed to save context: {} - {}", context.id, e.getMessage());
            return false;
        }
    }

    private void processDataPacks(MiapiEvents.ReloadEventData event) {
        if (event == null) {
            EditorLogger.getLogger().warn("Cannot process null event");
            return;
        }
        
        for (DataPackContext context : loadedPacks) {
            if (context == null) {
                continue;
            }
            
            if (context.enabled && !context.passedValidation) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(context.name + " Could not load, it did not pass Validation"));
            }
            if (!context.enabled || !context.passedValidation) continue;

            File dataDir = new File(context.directory, context.dataPath);
            if (!dataDir.exists() || !dataDir.isDirectory()) continue;

            try (Stream<Path> paths = Files.walk(dataDir.toPath())) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                ResourceLocation location = getResourceLocation(dataDir.toPath(), path);
                                String content = Files.readString(path);
                                if (shouldLoadJson(content)) {
                                    event.data.put(location, content);
                                }
                            } catch (IOException e) {
                                EditorLogger.getLogger().error("Failed to process file: {}", path.getFileName());
                            }
                        });
            } catch (IOException e) {
                EditorLogger.getLogger().error("Error walking data directory: {}", e.getMessage());
            }
        }
    }

    private boolean shouldLoadJson(String content) {
        try {
            JsonObject element = Miapi.gson.fromJson(content, JsonObject.class);
            if (!element.has("load_condition")) {
                return true;
            } else {
                boolean allowed = ConditionManager.get(element.get("load_condition")).isAllowed(new ConditionManager.ConditionContext() {
                    @Override
                    public ConditionManager.ConditionContext copy() {
                        return this;
                    }
                });
                if (allowed) {
                    element.remove("load_condition");
                    //EditorLogger.getLogger().info("redid " + location);
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    public boolean checkAndValidateDatapacks(boolean forced) {
        if (!forced && !isValidating && (!checkFileChanges() || ReloadEvents.isInReload())) {
            return false;
        }
        isValidating = true;
        List<MiapiEditor> editors = new ArrayList<>(openedEditors);
        editors.forEach(miapiEditor -> {
            if (miapiEditor instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            MiapiEditor.editors.remove(miapiEditor);
        });

        for (DataPackContext context : loadedPacks) {
            if (!context.enabled) continue;
            context.passedValidation = true;
            if (forced) {
                context.validatedFiles.clear();
            }
            File dataDir = new File(context.directory, context.dataPath);
            if (!dataDir.exists() || !dataDir.isDirectory()) {
                dataDir.mkdirs();
                continue;
            }

            try (Stream<Path> paths = Files.walk(dataDir.toPath())) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            File file = path.toFile();
                            String relativePath = dataDir.toPath().relativize(path).toString();

                            // Only process valid files
                            ValidationCache cache = context.getValidationCache(relativePath, file, forced);
                            if (cache.shouldLoad()) {
                                if (cache.blockLoad()) {
                                    try {
                                        String pathWithoutExt = relativePath.replace(".json", "").replace("\\", "/");
                                        pathWithoutExt = pathWithoutExt.replaceFirst("/", ":");
                                        ResourceLocation resourceLocation = Miapi.id(pathWithoutExt);
                                        String data = Files.readString(file.toPath());
                                        if (shouldLoadJson(data)) {
                                            var editor = new JsonEditor(Files.readString(file.toPath()), (f) -> {
                                            }, path, resourceLocation);
                                            editor.resourceLocation = resourceLocation;
                                            editor.closeOnNoError = true;
                                            MiapiEditor.editors.add(editor);
                                            openedEditors.add(editor);
                                        }
                                    } catch (RuntimeException e) {
                                        EditorLogger.getLogger().warn("", e);
                                    } catch (IOException e) {
                                        EditorLogger.getLogger().warn("", e);
                                    }
                                    context.passedValidation = false;
                                }
                            }
                        });
            } catch (IOException e) {
                EditorLogger.getLogger().warn("Failure during validation", e);
                isValidating = false;
                return false;
            }
        }
        for (DataPackContext context : loadedPacks) {
            if (!context.passedValidation) {
                isValidating = false;
                return false;
            }
        }
        isValidating = false;
        return true;
    }

    private ResourceLocation getResourceLocation(Path dataDir, Path filePath) {
        Path relativePath = dataDir.relativize(filePath);
        if (relativePath.getNameCount() < 2) return null;

        String namespace = relativePath.getName(0).toString().toLowerCase();
        String path = relativePath.subpath(1, relativePath.getNameCount())
                .toString()
                .replace('\\', '/').toLowerCase();

        return ResourceLocation.tryBuild(namespace, path);
    }

    public List<DataPackContext> getLoadedPacks() {
        // Return unmodifiable copy to prevent external modification
        return new ArrayList<>(loadedPacks);
    }

    @Override
    public void close() {
        EditorLogger.getLogger().info("Closing LiveDataPackManager");
        if (watchService != null) {
            try {
                watchService.close();
                watchService = null;
            } catch (IOException e) {
                EditorLogger.getLogger().error("Failed to close watch service: {}", e.getMessage());
            }
        }
        
        // Cancel all watch keys
        watchKeys.forEach((key, context) -> {
            key.cancel();
        });
        watchKeys.clear();
        
        // Close all opened editors
        openedEditors.forEach(editor -> {
            try {
                if (editor instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            } catch (Exception e) {
                EditorLogger.getLogger().error("Failed to close editor: {}", e.getMessage());
            }
        });
        openedEditors.clear();

        EditorLogger.getLogger().info("LiveDataPackManager closed successfully");
    }

    record ValidationCache(long lastModified, boolean shouldLoad, boolean blockLoad, List<EditorError> errors) {
    }
} 