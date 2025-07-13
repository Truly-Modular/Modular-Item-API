package smartin.miapi.editor;

import com.google.gson.*;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.editor.syntax.EditorInterface;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.cache.CacheCommands;
import smartin.miapi.modules.conditions.ConditionManager;
import smartin.miapi.modules.properties.util.EditorError;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class LiveDataPackManager implements AutoCloseable {
    private static final String RUNTIME_FOLDER = "miapi_runtime_datapacks";
    private static final String CONTEXT_FILE = "miapi-editor-context.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiveDataPackManager INSTANCE;

    private final List<DataPackContext> loadedPacks = new ArrayList<>();
    private final File runtimeFolder;
    private WatchService watchService;
    private final Map<WatchKey, DataPackContext> watchKeys = new HashMap<>();
    public List<MiapiEditor> openedEditors = new ArrayList<>();
    boolean isValidating = false;

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
            if (Platform.getEnv() == EnvType.CLIENT && Miapi.server != null) {
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

        } catch (IOException e) {
            e.printStackTrace();
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
                    Arrays.stream(dataDir.listFiles(File::isDirectory)).forEach(f -> {
                        watchDataPack(context, f);
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unwatchDataPack(DataPackContext context) {
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
                        } else {
                        }
                    }
                    key.reset();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                DataPackContext context = loadOrCreateContext(dir);
                loadedPacks.add(context);
                watchDataPack(context);
            }
        }
    }

    public DataPackContext createNewPack(String name, String id, String author, String description, boolean enabled) {
        id = id.toLowerCase();
        if (!name.isEmpty() && !id.isEmpty()) {
            File newDir = new File(runtimeFolder, id);
            if (!newDir.exists()) {
                newDir.mkdirs();
                DataPackContext context = createDefaultContext(newDir);
                context.name = name;
                context.id = id;
                context.author = author;
                context.description = description;
                context.enabled = enabled;
                context.dataPath = "data";
                saveContext(context);
                loadedPacks.add(context);
                watchDataPack(context);
                return context;
            }
        }
        return null;
    }

    public void deletePack(DataPackContext context) {
        try {
            unwatchDataPack(context);
            deleteDirectory(context.directory);
            loadedPacks.remove(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    Files.delete(file.toPath());
                }
            }
        }
        Files.delete(directory.toPath());
    }

    private DataPackContext loadOrCreateContext(File directory) {
        File contextFile = new File(directory, CONTEXT_FILE);
        DataPackContext context;

        if (contextFile.exists()) {
            try {
                String json = Files.readString(contextFile.toPath());
                context = GSON.fromJson(json, DataPackContext.class);
                context.directory = directory;
                context.repair();
            } catch (IOException e) {
                e.printStackTrace();
                context = createDefaultContext(directory);
            }
        } else {
            context = createDefaultContext(directory);
        }

        saveContext(context);
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

    public void saveContext(DataPackContext context) {
        try {
            File contextFile = new File(context.directory, CONTEXT_FILE);
            String json = GSON.toJson(context);
            Files.writeString(contextFile.toPath(), json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processDataPacks(MiapiEvents.ReloadEventData event) {
        for (DataPackContext context : loadedPacks) {
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
                                if (shouldLoadJson(Files.readString(path))) {
                                    event.data.put(location, content);
                                }
                            } catch (IOException e) {
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
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
                    //Miapi.LOGGER.info("redid " + location);
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
                                        Miapi.LOGGER.warn("", e);
                                    } catch (IOException e) {
                                        Miapi.LOGGER.warn("", e);
                                    }
                                    context.passedValidation = false;
                                }
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
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
        return new ArrayList<>(loadedPacks);
    }

    @Override
    public void close() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class DataPackContext {
        public String name;
        public String id;
        public String author;
        public String description;
        public boolean enabled;
        public String dataPath;
        public boolean watchFiles = true;
        public transient File directory;
        public transient boolean passedValidation = true;
        public transient Map<String, ValidationCache> validatedFiles = new HashMap<>();

        public void repair() {
            if (dataPath == null) {
                dataPath = "data";
            }
            if (validatedFiles == null) {
                validatedFiles = new HashMap<>();
            }
        }

        @Nullable
        public ValidationCache getValidationCache(String relativePath, File file, boolean forced) {
            ValidationCache cache = validatedFiles.get(relativePath);
            if (cache == null || cache.lastModified != file.lastModified() || !cache.blockLoad() && forced) {
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
                            List<EditorError> errors = iface.validateContent(json, content);
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
                        cache = new ValidationCache(lastModified, shouldLoad, !(shouldLoad && isValid), allErrors);
                        if (cache.blockLoad()) {
                            Miapi.LOGGER.warn("could not validate file " + file.toPath());
                        }
                        validatedFiles.put(relativePath, cache);
                    } else {
                        long lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
                        cache = new ValidationCache(lastModified, shouldLoad, false, List.of());
                        validatedFiles.put(relativePath, cache);
                    }

                } catch (Exception e) {
                    // If any error occurs during validation, consider the file invalid
                    long lastModified = 0;
                    try {
                        lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
                    } catch (IOException ex) {
                        Miapi.LOGGER.warn("", ex);
                    }
                    cache = new ValidationCache(lastModified, false, false, List.of(new EditorError(0, "Critical load issue," + e.getMessage(), EditorError.ErrorSeverity.ERROR)));
                    validatedFiles.put(relativePath, cache);
                }
            }
            boolean isValid = cache != null && cache.shouldLoad();
            if (!isValid) {
                Miapi.LOGGER.warn("fail");
            }
            return cache;
        }

        public boolean isFileValid(String relativePath, File file) {
            ValidationCache cache = getValidationCache(relativePath, file, false);
            boolean isValid = cache != null && cache.shouldLoad();
            if (!isValid) {
                Miapi.LOGGER.warn("fail");
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

    private record ValidationCache(long lastModified, boolean shouldLoad, boolean blockLoad, List<EditorError> errors) {
    }
} 