package smartin.miapi.fabric;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.conditions.ConditionManager;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class MiapiReloadListener implements PreparableReloadListener {
    Supplier<RegistryAccess> registryAccess;
    static long timeStart;

    public MiapiReloadListener(Supplier<RegistryAccess> registryAccess) {
        this.registryAccess = registryAccess;
    }

    public CompletableFuture load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        ReloadEvents.reloadCounter++;
        timeStart = System.nanoTime();
        ReloadEvents.START.fireEvent(false, registryAccess.get());
        Map<ResourceLocation, String> data = new LinkedHashMap<>();

        ReloadEvents.syncedPaths.forEach((modID, dataPaths) -> {
            dataPaths.forEach(dataPath -> {
                Map<ResourceLocation, List<Resource>> map = manager.listResourceStacks(dataPath, (fileName) -> true);
                map.forEach((identifier, resources) -> {
                    resources.forEach(resource -> {
                        try {
                            BufferedReader reader = resource.openAsReader();
                            String dataString = reader.lines().collect(Collectors.joining());
                            data.put(identifier, dataString);
                        } catch (Exception e) {
                            Miapi.LOGGER.warn("Error Loading Resource" + identifier + " " + resources);
                        }
                    });
                });
            });
        });
        return CompletableFuture.completedFuture(data);
    }

    public CompletableFuture<Void> apply(Object data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> {
            Map<ResourceLocation, String> dataMap = new HashMap<>((Map) data);
            Map<ResourceLocation, String> filteredMap = new HashMap<>();
            dataMap.forEach((key, value) -> {
                if (!key.getPath().endsWith(".json")) {
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
            //Miapi.registryAccess = Miapi.server.reloadableRegistries().get();
            ReloadEvents.MAIN.fireEvent(false, registryAccess.get());
            ReloadEvents.END.fireEvent(false, registryAccess.get());
            Miapi.LOGGER.info("Server load took " + (double) (System.nanoTime() - timeStart) / 1000 / 1000 + " ms");
            if (Miapi.server != null) {
                Miapi.server.getPlayerList().getPlayers().forEach(ReloadEvents::triggerReloadOnClient);
            }
            ReloadEvents.reloadCounter--;
        });
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return load(resourceManager, preparationsProfiler, backgroundExecutor)
                .thenCompose(preparationBarrier::wait)
                .thenAcceptAsync(a -> {
                    apply(a, resourceManager, reloadProfiler, gameExecutor);
                });
    }
}