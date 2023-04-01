package smartin.miapi.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientSync {
    protected static Map<String,String> dataPacks = new HashMap<>();

    public static void init(){
        if(Environment.isClient()){
            clientInit();
        }
        serverInit();
    }

    protected static void serverInit(){
        ReloadEvent.Data.subscribe((path, data)->{
            dataPacks.put(path,data);
        });
        Miapi.LOGGER.info("Server innit Networking");
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender , minecraftServer)->{
            onPlayerConnect(serverPlayNetworkHandler.player);
        });
    }

    protected static void clientInit(){
        Miapi.LOGGER.info("Client innit Networking");
    }

   protected static void onPlayerConnect(ServerPlayerEntity entity){
        if(Environment.isClient()){
            if((MinecraftClient.getInstance().player)==null) {
                //this is a bad way to ensure this doesnt send to itself....
                return;
            }
        }
        ReloadEvent.triggerReloadOnClient(entity);
    }
}
