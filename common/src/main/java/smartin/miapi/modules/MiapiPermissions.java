package smartin.miapi.modules;

import net.minecraft.entity.player.PlayerEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class MiapiPermissions {
    static HttpClient httpClient = HttpClients.createDefault();
    static WeakHashMap<PlayerEntity,List<String>> playerPerms = new WeakHashMap<>();

    public static boolean hasPerm(PlayerEntity player,String perm){
        if(MiapiConfig.isDevelopment()){
            return true;
        }
        return MiapiPermissions.getPerms(player).contains(perm);
    }

    public static boolean hasPerm(PlayerEntity player,List<String> perms){
        for(String perm:perms){
            if(hasPerm(player,perm)){
                return true;
            }
        }
        return false;
    }

    public static List<String> getPerms(PlayerEntity player){
        if(playerPerms.containsKey(player)){
            return playerPerms.get(player);
        }
        List<String> perms = getPerms(player.getUuid());
        playerPerms.put(player,perms);
        return perms;
    }

    public static List<String> getPerms(UUID playerUUID){
        HttpGet httpGet = new HttpGet("http://trulymodular.duckdns.org:3000/perms/"+playerUUID.toString());
        try {
            HttpResponse response = httpClient.execute(httpGet);

            String responseBody = EntityUtils.toString(response.getEntity());
            Miapi.LOGGER.warn("Response: " + responseBody);
            return Miapi.gson.fromJson(responseBody, PermissionJson.class).permissions;
        } catch (Exception e) {
            Miapi.LOGGER.error("Could not resolve Miapi Permissions ",e);
            return new ArrayList<>();
        }
    }

    private static class PermissionJson {
        public String uuid;
        public List<String> permissions;
    }
}
