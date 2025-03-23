package smartin.miapi.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LootModifierManager implements PreparableReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Logger LOGGER = LogManager.getLogger();

    public static List<ResourceLocation> toLoad = List.of();
    public static List<LootTable> finishedPools = null;
    private static LayeredRegistryAccess<RegistryLayer> access;

    public LootModifierManager() {
    }

    public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
        }, gameExecutor);
    }

    public static List<ResourceLocation> prepare(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> newAccess) {
        List<ResourceLocation> finalLocations = new ArrayList<>();
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("miapi", "global_loot_modifiers/tables.json");
        for (Resource resource : resourceManager.getResourceStack(resourceLocation)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject jsonobject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                boolean replace = GsonHelper.getAsBoolean(jsonobject, "replace", false);
                if (replace)
                    finalLocations.clear();
                JsonArray entries = GsonHelper.getAsJsonArray(jsonobject, "entries");
                for (int i = 0; i < entries.size(); i++) {
                    ResourceLocation loc = ResourceLocation.parse(GsonHelper.convertToString(entries.get(i), "entries[" + i + "]"));
                    finalLocations.remove(loc);
                    finalLocations.add(loc);
                }
            } catch (RuntimeException | IOException ioexception) {
                LOGGER.error("Couldn't read global loot modifier list {} in data pack {}", resourceLocation, resource.sourcePackId(), ioexception);
            }
        }
        access = newAccess;
        toLoad = finalLocations;
        return finalLocations;
    }

    protected void apply(List<ResourceLocation> resourceList, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        toLoad = resourceList;
        finishedPools = null;
        LootTable table;
    }

    public static List<LootTable> getLootPools() {
        if (finishedPools == null) {
            finishedPools = toLoad.stream().map(e -> access.getLayer(RegistryLayer.RELOADABLE).registry(Registries.LOOT_TABLE).get().get(e)).filter(Objects::nonNull).toList();
        }
        return finishedPools;
    }
}

