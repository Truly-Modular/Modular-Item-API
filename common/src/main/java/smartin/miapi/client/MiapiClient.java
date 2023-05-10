package smartin.miapi.client;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import net.minecraft.client.MinecraftClient;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.CustomColorProvider;
import smartin.miapi.mixin.ItemRendererAccessor;

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
    }
}
