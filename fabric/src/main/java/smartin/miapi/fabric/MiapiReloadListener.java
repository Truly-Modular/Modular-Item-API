package smartin.miapi.fabric;

import com.google.gson.JsonObject;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.conditions.ConditionManager;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class MiapiReloadListener implements ResourceReloader {
    static long timeStart;

    public CompletableFuture load(ResourceManager manager, Profiler profiler, Executor executor) {
        ReloadEvents.reloadCounter++;
        timeStart = System.nanoTime();
        ReloadEvents.START.fireEvent(false);
        Map<String, String> data = new LinkedHashMap<>();

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
        return CompletableFuture.completedFuture(data);
    }

    public CompletableFuture<Void> apply(Object data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Map<String, String> dataMap = new HashMap<>((Map) data);
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

                        @Override
                        public List<Text> getReasons() {
                            return new ArrayList<>();
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
                Miapi.server.getPlayerManager().getPlayerList().forEach(ReloadEvents::triggerReloadOnClient);
            }
            ReloadEvents.reloadCounter--;
        });
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return load(manager, prepareProfiler, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenAcceptAsync(a -> {
            apply(a, manager, applyProfiler, applyExecutor);
        });
    }
}