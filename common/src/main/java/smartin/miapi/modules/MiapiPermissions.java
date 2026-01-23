package smartin.miapi.modules;

import net.minecraft.world.entity.player.Player;
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
    static Set<UUID> loadingPerms = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static boolean hasPerm(Player player, String perm) {
        if (MiapiConfig.getServerConfig().other.developmentMode) {
            return true;
        }
        if (perm.equals(player.getUUID().toString())) {
            return true;
        }

        List<String> perms = getPerms(player);
        return perms.contains(perm) || perms.contains("broken");
    }

    public static boolean hasPerm(Player player, List<String> perms) {
        for (String perm : perms) {
            if (hasPerm(player, perm)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> getPerms(Player player) {
        UUID uuid = player.getUUID();
        if (playerPerms.containsKey(uuid)) {
            return playerPerms.get(uuid);
        }

        // Temporary permission until fetched
        List<String> defaultPerms = new ArrayList<>(List.of("user"));
        playerPerms.put(uuid, defaultPerms);

        if (!loadingPerms.contains(uuid)) {
            loadingPerms.add(uuid);
            fetchPermissionsAsync(uuid);
        }

        return defaultPerms;
    }

    private static void fetchPermissionsAsync(UUID playerUUID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://trulymodular.dedyn.io:3000/perms/" + playerUUID))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        PermissionJson perms = Miapi.gson.fromJson(responseBody, PermissionJson.class);
                        if (perms != null && perms.permissions != null) {
                            perms.permissions.add("user");
                            playerPerms.put(playerUUID, perms.permissions);
                        } else {
                            playerPerms.put(playerUUID, new ArrayList<>(List.of("broken", "user")));
                        }
                    } catch (Exception e) {
                        Miapi.LOGGER.warn("Failed to parse Miapi Permissions for UUID " + playerUUID);
                        playerPerms.put(playerUUID, new ArrayList<>(List.of("broken", "user")));
                    } finally {
                        loadingPerms.remove(playerUUID);
                    }
                })
                .exceptionally(e -> {
                    Miapi.LOGGER.warn("Could not retrieve Miapi Permissions for UUID " + playerUUID);
                    playerPerms.put(playerUUID, new ArrayList<>(List.of("broken", "user")));
                    loadingPerms.remove(playerUUID);
                    return null;
                });
    }

    private static class PermissionJson {
        public String uuid;
        public List<String> permissions;
    }
}
