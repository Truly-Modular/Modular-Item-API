package smartin.miapi.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class IdentifiableMiapiReloadListenerFixed implements IdentifiableResourceReloadListener {
    MiapiReloadListener listener = new MiapiReloadListener(() -> {
        if (Miapi.server != null) {
            Miapi.registryAccess = Miapi.server.reloadableRegistries().get();
            return Miapi.server.registryAccess();
        }
        return null;
    });

    @Override
    public ResourceLocation getFabricId() {
        return Miapi.id("main_reload_listener");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return List.of(ResourceReloadListenerKeys.TAGS, ResourceReloadListenerKeys.RECIPES);
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return listener.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
    }
}
