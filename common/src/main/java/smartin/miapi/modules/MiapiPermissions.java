package smartin.miapi.modules;

import net.minecraft.entity.player.PlayerEntity;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MiapiPermissions {

    static HttpClient httpClient = HttpClient.newHttpClient();

    static Map<UUID, List<String>> playerPerms = new ConcurrentHashMap<>();
    static Set<UUID> requestsInFlight = ConcurrentHashMap.newKeySet();

    public static boolean hasPerm(PlayerEntity player, String perm) {
        if (MiapiConfig.INSTANCE.server.other.developmentMode) return true;

        if (perm.equals(player.getUuid().toString())) return true;

        try {
            List<String> perms = getPerms(player);
            return perms.contains(perm) || perms.contains("broken");
        } catch (Exception ignored) {
            return true;
        }
    }

    public static boolean hasPerm(PlayerEntity player, List<String> perms) {
        for (String perm : perms) {
            if (hasPerm(player, perm)) return true;
        }
        return false;
    }

    public static List<String> getPerms(PlayerEntity player) {
        UUID uuid = player.getUuid();

        List<String> perms = playerPerms.get(uuid);
        if (perms != null) return perms;

        // immediately allow everything while loading
        List<String> broken = new ArrayList<>(List.of("broken", "user"));
        playerPerms.put(uuid, broken);

        requestPermsAsync(uuid);

        return broken;
    }

    private static void requestPermsAsync(UUID uuid) {
        if (!requestsInFlight.add(uuid)) return;
        requestsInFlight.add(uuid);

        URI uri = URI.create("http://trulymodular.dedyn.io:3000/perms/" + uuid);

        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    try {
                        PermissionJson perms = Miapi.gson.fromJson(body, PermissionJson.class);

                        List<String> list = new ArrayList<>(perms.permissions);
                        list.add("user");

                        playerPerms.put(uuid, list);
                    } catch (Exception e) {
                        Miapi.LOGGER.warn("Failed to parse Miapi permissions");
                    } finally {
                        requestsInFlight.remove(uuid);
                    }
                })
                .exceptionally(ex -> {
                    Miapi.LOGGER.warn("Couldnt retrieve Miapi Permissions");
                    requestsInFlight.remove(uuid);
                    return null;
                });
    }

    private static class PermissionJson {
        public String uuid;
        public List<String> permissions;
    }
}