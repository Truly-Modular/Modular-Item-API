package smartin.miapi.datapack;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import dev.architectury.platform.Platform;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.modules.conditions.ConditionManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class MiapiReloadListener implements ResourceReloader {
    static long timeStart;

    public CompletableFuture load(ResourceManager manager, Profiler profiler, Executor executor) {
        ReloadEvents.inReload = true;
        timeStart = System.nanoTime();
        ReloadEvents.START.fireEvent(false);
        Map<String, String> data = new LinkedHashMap<>();
        TypeToken<String> placeHolderToken = TypeToken.get(String.class);
        com.google.common.reflect.TypeToken<String> secondPlaceHolder = com.google.common.reflect.TypeToken.of(String.class);

        if (Platform.isFabric() && false) {
            //TODO:figure out why this does nto work on forge.
            manager.streamResourcePacks().forEach(resourcePack -> {
                Miapi.DEBUG_LOGGER.error("loaded " + resourcePack.getName() + " DataPack");
                ReloadEvents.syncedPaths.keySet().forEach(nameSpace -> {
                    Miapi.DEBUG_LOGGER.warn("checking Namespace " + nameSpace);
                    resourcePack.findResources(ResourceType.SERVER_DATA, nameSpace, "", (identifier, inputSupplier) -> {
                        Miapi.DEBUG_LOGGER.warn("checking " + identifier);
                        if (ReloadEvents.syncedPaths.get(nameSpace).stream().anyMatch(path -> identifier.getPath().startsWith(path))) {
                            Miapi.DEBUG_LOGGER.warn("Loading " + identifier);
                            try {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputSupplier.get(), StandardCharsets.UTF_8));
                                String dataString = reader.lines().collect(Collectors.joining());
                                String fullPath = identifier.getPath();
                                data.put(fullPath, dataString);
                            } catch (Exception e) {
                                Miapi.LOGGER.warn("Error Loading Resource" + identifier);
                            }
                        }
                    });
                });
            });
        } else {
            ReloadEvents.syncedPaths.forEach((modID, dataPaths) -> {
                dataPaths.forEach(dataPath -> {
                    Map<Identifier, List<Resource>> map = manager.findAllResources(dataPath, (fileName) -> true);
                    map.forEach((identifier, resources) -> {
                        if (identifier.getNamespace().equals(modID)) {
                            resources.forEach(resource -> {
                                try {
                                    BufferedReader reader = resource.getReader();
                                    String dataString = reader.lines().collect(Collectors.joining());
                                    String fullPath = identifier.getPath();
                                    data.put(fullPath, dataString);
                                } catch (Exception e) {
                                    Miapi.LOGGER.warn("Error Loading Resource" + identifier + " " + resources);
                                }
                            });
                        }
                    });
                });
            });
        }
        return CompletableFuture.completedFuture(data);
    }

    public CompletableFuture<Void> apply(Object data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Map<String, String> dataMap = (Map) data;
            Map<String, String> filteredMap = new HashMap<>();
            dataMap.forEach((key, value) -> {
                if (!key.endsWith(".json")) {
                    filteredMap.put(key, value);
                    return;
                }
                try {
                    JsonObject element = Miapi.gson.fromJson(value, JsonObject.class);
                    if (!element.has("load_condition")) {
                        filteredMap.put(key, value);
                        return;
                    }
                    boolean allowed = ConditionManager.get(element.get("load_condition")).isAllowed(new ConditionManager.ConditionContext() {
                        @Override
                        public ConditionManager.ConditionContext copy() {
                            return this;
                        }
                    });
                    if (allowed) {
                        element.remove("load_condition");
                        Miapi.LOGGER.info("redid " + key);
                        filteredMap.put(key, Miapi.gson.toJson(element));
                    }
                } catch (Exception e) {
                    filteredMap.put(key, value);
                }
            });


            ReloadEvents.DataPackLoader.trigger(filteredMap);
            ReloadEvents.MAIN.fireEvent(false);
            ReloadEvents.END.fireEvent(false);
            Miapi.LOGGER.info("Server load took " + (double) (System.nanoTime() - timeStart) / 1000 / 1000 + " ms");
            if (Miapi.server != null) {
                Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    ReloadEvents.triggerReloadOnClient(serverPlayerEntity);
                });
            }
            ReloadEvents.inReload = false;
        });
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return load(manager, prepareProfiler, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync( a ->{
            apply(a, manager, applyProfiler, applyExecutor);
        });
        /*
        return load(manager, prepareProfiler, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenCompose(
                (o) -> apply(o, manager, applyProfiler, applyExecutor)
        );
         */
    }
}