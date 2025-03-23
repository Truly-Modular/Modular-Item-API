package smartin.miapi.mixin;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerRegistries.class)
public class ReloadAbleServerRegistriesMixin {
    @Inject(
            method = "reload(Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "HEAD"), cancellable = true)
    private static void miapi$registerPacket(LayeredRegistryAccess<RegistryLayer> registries, ResourceManager resourceManager, Executor backgroundExecutor, CallbackInfoReturnable<CompletableFuture<LayeredRegistryAccess<RegistryLayer>>> cir) {
        try {
            //LootModifierManager.prepare(resourceManager, registries);
        } catch (RuntimeException e) {

        }
    }
}
