package smartin.miapi.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.mixin.client.ItemRendererAccessor;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectileRenderer;

public class MiapiClient {

    private MiapiClient() {
    }

    public static void init(){
        ClientLifecycleEvent.CLIENT_SETUP.register(MiapiClient::clientSetup);
        ClientLifecycleEvent.CLIENT_STARTED.register(MiapiClient::clientStart);
    }

    protected static void clientSetup(MinecraftClient client){
        SpriteLoader.setup();
    }

    protected static void clientStart(MinecraftClient client){
        Miapi.itemRegistry.addCallback(item -> {
            ((ItemRendererAccessor) client.getItemRenderer()).color().register(new CustomColorProvider(), item);
        });
        EntityRendererRegistry.register(() -> {
            return Miapi.ItemProjectile;
        }, (context) -> {
            return new ItemProjectileRenderer(context);
        });
    }
}
