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

    NetworkingImplFabric networkingImplFabric;

    @Override
    public void onInitialize() {
        Miapi.init();
        //NETWORKING
        networkingImplFabric = new NetworkingImplFabric();
        Networking.setImplementation(networkingImplFabric);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }
        Miapi.entityAttributeRegistry.addCallback(item -> {
            Registry.register(Registries.ATTRIBUTE, new Identifier(Objects.requireNonNull(Miapi.entityAttributeRegistry.findKey(item))), item);
        });
        Miapi.modularItemRegistry.addCallback(item -> {
            Registry.register(Registries.ITEM, new Identifier(Objects.requireNonNull(Miapi.modularItemRegistry.findKey(item))), item);
        });
        Miapi.blockItemRegistry.addCallback(item -> {
            Registry.register(Registries.BLOCK, new Identifier(Objects.requireNonNull(Miapi.blockItemRegistry.findKey(item))), item);
            Registry.register(Registries.ITEM, new Identifier(Objects.requireNonNull(Miapi.blockItemRegistry.findKey(item))), new BlockItem(item, new Item.Settings()));
        });
    }

    private void onServerStart(MinecraftServer minecraftServer) {
        networkingImplFabric.setupServer();
    }
}