package smartin.miapi.fabric;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.conditions.ConditionManager;

import java.io.BufferedReader;
import java.util.*;
import java.util.stream.Collectors;

import static smartin.miapi.Miapi.MOD_ID;


public class MiapiClientReloadListenerFabric extends SinglePreparationResourceReloader<Map<String, String>> implements IdentifiableResourceReloadListener {

    protected void apply(Map<String, String> prepared, ResourceManager manager, Profiler profiler) {
        Map<String, String> dataMap = new HashMap<>((Map) prepared);
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


        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().world != null) {
            ReloadEvents.executeClientReload(MinecraftClient.getInstance(), filteredMap);
        } else {
            ReloadEvents.CLIENT_DATA_PACKS.clear();
            ReloadEvents.CLIENT_DATA_PACKS.putAll(filteredMap);
        }
    }

    @Override
    protected Map<String, String> prepare(ResourceManager manager, Profiler profiler) {
        Map<String, String> data = new LinkedHashMap<>();

        Map<String, List<String>> synced = new HashMap<>(ReloadEvents.syncedPaths);
        synced.forEach((modID, dataPaths) -> {
            new ArrayList<>(dataPaths).forEach(dataPath -> {
                Map<Identifier, List<Resource>> map = manager.findAllResources("miapi_datapack_data/" + dataPath, (fileName) -> true);
                map.forEach((identifier, resources) -> {
                    if (identifier.getNamespace().equals(modID) && identifier.getPath().startsWith("miapi_datapack_data/")) {
                        resources.forEach(resource -> {
                            try {
                                BufferedReader reader = resource.getReader();
                                String dataString = reader.lines().collect(Collectors.joining());
                                String fullPath = identifier.getPath().replace("miapi_datapack_data/", "");
                                data.put(fullPath, dataString);
                            } catch (Exception e) {
                                Miapi.LOGGER.warn("Error Loading Resource" + identifier + " " + resources);
                            }
                        });
                    }
                });
            });
        });
        return data;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(MOD_ID, "client_main_reload_listener");
    }
}