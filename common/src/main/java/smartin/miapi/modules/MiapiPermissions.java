package smartin.miapi.modules;

import net.minecraft.entity.player.PlayerEntity;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class MiapiPermissions {
    static HttpClient httpClient = HttpClient.newHttpClient();
    static WeakHashMap<PlayerEntity, List<String>> playerPerms = new WeakHashMap<>();

    public static boolean hasPerm(PlayerEntity player, String perm) {
        if (MiapiConfig.INSTANCE.server.other.developmentMode) {
            return true;
        }
        if(perm.equals(player.getUuid().toString())){
            return true;
        }
        try {
            List<String> perms = MiapiPermissions.getPerms(player);
            return perms.contains(perm) || perms.contains("broken");
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean hasPerm(PlayerEntity player, List<String> perms) {
        for (String perm : perms) {
            if (hasPerm(player, perm)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getPerms(PlayerEntity player) {
        if (playerPerms.containsKey(player)) {
            return playerPerms.get(player);
        }
        List<String> perms = getPerms(player.getUuid());
        perms.add("user");
        playerPerms.put(player, perms);
        return perms;
    }

    public static List<String> getPerms(UUID playerUUID) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://trulymodular.duckdns.org:3000/perms/" + playerUUID.toString()));
        builder.GET();
        URI uri = URI.create("http://trulymodular.duckdns.org:3000/perms/" + playerUUID.toString());
        builder.uri(uri);
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PermissionJson perms = Miapi.gson.fromJson(response.body(), PermissionJson.class);
            return perms.permissions;
        } catch (Exception suppressed) {
            Miapi.LOGGER.warn("Couldnt retrieve Miapi Permissions");
            return new ArrayList<>(List.of("broken"));
        }
    }

    private static class PermissionJson {
        public String uuid;
        public List<String> permissions;
    }
}
