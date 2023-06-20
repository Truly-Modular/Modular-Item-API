package smartin.miapi.datapack;

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class ReloadListener implements ResourceReloader {

    public CompletableFuture load(ResourceManager manager, Profiler profiler, Executor executor) {
        ReloadEvents.inReload = true;
        ReloadEvents.START.fireEvent(false);
        Map<String,String> data = new HashMap<>();
        ReloadEvents.syncedPaths.forEach((modID,dataPaths)->{
            dataPaths.forEach(dataPath->{
                Map<Identifier, List<Resource>> map = manager.findAllResources(dataPath, (fileName) -> true);
                map.forEach((identifier, resources) -> {
                    if(identifier.getNamespace().equals(modID)){
                        resources.forEach(resource -> {
                            try{
                                BufferedReader reader = resource.getReader();
                                String dataString = reader.lines().collect(Collectors.joining());
                                String fullPath = identifier.getPath();
                                data.put(fullPath,dataString);
                            }
                            catch (Exception e){
                                Miapi.LOGGER.warn("Error Loading Resource"+identifier+""+ resources);
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
            Map<String,String> dataMap = (Map) data;
            dataMap.forEach(ReloadEvents.DataPackLoader::trigger);
            ReloadEvents.MAIN.fireEvent(false);
            ReloadEvents.END.fireEvent(false);
            if(Miapi.server!=null){
                Miapi.server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
                    ReloadEvents.triggerReloadOnClient(serverPlayerEntity);
                });
            }
            ReloadEvents.inReload = false;
        });
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return load(manager, prepareProfiler, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenCompose(
                (o) -> apply(o, manager, applyProfiler, applyExecutor)
        );
    }
}