package smartin.miapi.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.event.EventResult;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.events.MiapiEvents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LiveDataPackManager {
    private static final String RUNTIME_FOLDER = "miapi_runtime_datapacks";
    private static final String CONTEXT_FILE = "miapi-editor-context.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static LiveDataPackManager INSTANCE;

    private final List<DataPackContext> loadedPacks = new ArrayList<>();
    private final File runtimeFolder;

    public static void setup() {
        MiapiEvents.ADJUST_RAW_DATA.register(event -> {
            getInstance().processDataPacks(event);
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
        scanForDataPacks();
    }

    private File getRuntimeFolder() {
        return FabricLoader.getInstance().getGameDir().resolve(RUNTIME_FOLDER).toFile();
    }

    public void scanForDataPacks() {
        loadedPacks.clear();
        File[] directories = runtimeFolder.listFiles(File::isDirectory);

        if (directories != null) {
            for (File dir : directories) {
                DataPackContext context = loadOrCreateContext(dir);
                loadedPacks.add(context);
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
                saveContext(context);
                loadedPacks.add(context);
                return context;
            }
        }
        return null;
    }

    public void deletePack(DataPackContext context) {
        try {
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
            if (!context.enabled) continue;

            File dataDir = new File(context.directory, "data");
            if (!dataDir.exists() || !dataDir.isDirectory()) continue;

            try (Stream<Path> paths = Files.walk(dataDir.toPath())) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                ResourceLocation location = getResourceLocation(dataDir.toPath(), path);
                                if (location != null) {
                                    event.data.put(location, content);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ResourceLocation getResourceLocation(Path dataDir, Path filePath) {
        Path relativePath = dataDir.relativize(filePath);
        if (relativePath.getNameCount() < 2) return null;

        String namespace = relativePath.getName(0).toString();
        String path = relativePath.subpath(1, relativePath.getNameCount())
                .toString()
                .replace('\\', '/')
                .replaceAll("\\.json$", "");

        return ResourceLocation.parse(namespace + ":" + path);
    }

    public List<DataPackContext> getLoadedPacks() {
        return new ArrayList<>(loadedPacks);
    }

    public static class DataPackContext {
        public String name;
        public String id;
        public String author;
        public String description;
        public boolean enabled;
        public transient File directory;
    }
} 