package smartin.miapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

public class TrulyModularFabric implements ModInitializer {

    NetworkingImplFabric networkingImplFabric;

    @Override
    public void onInitialize() {
        Miapi.init();

        ClientSync.init();
        //NETWORKING
        networkingImplFabric = new NetworkingImplFabric();
        Networking.setImplementation(networkingImplFabric);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        networkingImplFabric.setupServer();
    }
}