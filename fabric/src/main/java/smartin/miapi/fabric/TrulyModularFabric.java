package smartin.miapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

import java.util.Objects;

public class TrulyModularFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }
    }
}