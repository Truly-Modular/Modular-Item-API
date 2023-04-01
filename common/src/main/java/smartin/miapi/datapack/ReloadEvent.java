package smartin.miapi.datapack;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.network.Networking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReloadEvent {
    protected static final List<EventListener> startListeners = new ArrayList<>();
    protected static final List<EventListener> endListeners = new ArrayList<>();
    protected static final String reloadPacketId = Miapi.MOD_ID+":events_reload_s2c";
    protected static final String reloadDataPacketId = Miapi.MOD_ID+":events_reload_s2c_data";
    protected static Map<String,String> dataPacks = new HashMap<>();

    public static void setup(){
        if(Environment.isClient()){
            clientSetup();
        }
        ReloadEvent.Data.subscribe((path, data)->{
            dataPacks.put(path,data);
        });
    }

    public static void triggerReloadOnClient(ServerPlayerEntity entity){
        PacketByteBuf buf = Networking.createBuffer();
        buf.writeBoolean(true);
        Networking.sendS2C(reloadPacketId,buf);
        dataPacks.forEach((key,data)->{
            PacketByteBuf buffer = Networking.createBuffer();
            buffer.writeString(key);
            buffer.writeString(data);
            Networking.sendS2C(reloadDataPacketId,entity,buffer);
            Miapi.LOGGER.info("Client Connected");
        });
        PacketByteBuf outBuf = Networking.createBuffer();
        outBuf.writeBoolean(false);
        Networking.sendS2C(reloadPacketId,outBuf);
    }

    private static void clientSetup(){
        Networking.registerS2CPacket(reloadDataPacketId,(buffer)->{
            String key = buffer.readString();
            String data = buffer.readString();
            ReloadEvent.Data.trigger(key,data);
        });
        Networking.registerS2CPacket(reloadPacketId,(buffer)->{
            Boolean isStart = buffer.readBoolean();
            Miapi.LOGGER.info(String.valueOf(isStart));
            triggerReloadEvent(isStart,true);
        });
    }

    public static void reloadEventTriggerServer(boolean isStart){
        triggerReloadEvent(isStart,false);
        if(Miapi.server==null) return;
        if(!isStart){
            Miapi.server.getPlayerManager().getPlayerList().forEach(player->{
                triggerReloadOnClient(player);
            });
        }
    }

    private static void triggerReloadEvent(boolean isStart, boolean isClient){
        Miapi.LOGGER.info("reload start "+isStart+" client "+isClient);
        if(isStart){
            startListeners.forEach(eventListener -> {
                eventListener.onEvent(isClient);
            });
        }
        else{
            endListeners.forEach(eventListener -> {
                eventListener.onEvent(isClient);
            });
        }
    }

    public static void subscribeStart(EventListener listener) {
        startListeners.add(listener);
    }

    public static void unsubscribeStart(EventListener listener) {
        startListeners.remove(listener);
    }

    public static void subscribeEnd(EventListener listener) {
        endListeners.add(listener);
    }

    public static void unsubscribeEnd(EventListener listener) {
        endListeners.remove(listener);
    }

    public interface EventListener {
        void onEvent(boolean isClient);
    }

    public static class Data {
        protected static final List<EventListener> listeners = new ArrayList<>();

        /*
        This event Triggers by default once on server start on the server, and once per clientConnect on the Client
         */
        public static void subscribe(EventListener listener) {
            listeners.add(listener);
        }

        public static void unsubscribe(EventListener listener) {
            listeners.remove(listener);
        }

        /*
        It is not recommended to manually trigger this event.
        If you would like to have a reload trigger the reload event on the server
         */
        public static void trigger(String path, String data) {
            for (EventListener listener : listeners) {
                listener.onEvent(path, data);
            }
        }

        public interface EventListener {
            void onEvent(String path, String data);
        }
    }
}