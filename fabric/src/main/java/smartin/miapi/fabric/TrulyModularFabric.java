package smartin.miapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.network.Networking;

public class TrulyModularFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        ReloadEvents.DataPackLoader.subscribe(((path, data) -> {
            //Miapi.LOGGER.info(path);
            //Miapi.LOGGER.warn(data);
        }));
        ClientSync.init();
        Miapi.itemRegistry.addCallback(item ->   {
            //Registry.register(Registry.ITEM, Miapi.modularItemIdentifier, item);
            Miapi.LOGGER.warn(Miapi.itemRegistry.findKey(item));
            Registry.register(Registry.ITEM, new Identifier(Miapi.itemRegistry.findKey(item)), item);
            Miapi.LOGGER.info("registered Item successfully");
        });
        //NETWORKING
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }
    }

    public static ModelLoader getModelLoader() {
        return null;
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        Networking.setImplementation(new NetworkingImplFabric());
    }
}