package smartin.miapi.registries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import smartin.miapi.Miapi;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * this is stuff for internal datapacks,
 * a common abstraction.
 * it works automaticly for stuff in resource/resourcepacks
 * (works for resource packs and not just datapacks.
 * register mod at {@link smartin.miapi.datapack.ReloadEvents#MOD_IDS_TO_SCAN} to be scanned for viable datapacks.
 * needs to be registered prior to datapack loading.
 */
public record DatapackHolder(Component forgeName, boolean defaultEnabled) {

    public static boolean shouldEnableByDefault(Path packDir) {
        Path metaPath = packDir.resolve("pack.mcmeta");

        if (!Files.exists(metaPath)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(metaPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // look for your custom key at root level
            if (json.has("miapi_default_enable")) {
                return json.get("miapi_default_enable").getAsBoolean();
            }

        } catch (Exception e) {
            Miapi.LOGGER.error("Failed reading pack.mcmeta at {}", metaPath, e);
        }

        return false;
    }

    public static String getDefaultName(Path packDir, String fallback) {
        Path metaPath = packDir.resolve("pack.mcmeta");

        if (!Files.exists(metaPath)) {
            return fallback;
        }

        try (BufferedReader reader = Files.newBufferedReader(metaPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // look for your custom key at root level
            if (json.has("miapi_visible_name")) {
                return json.get("miapi_visible_name").getAsString();
            }

        } catch (Exception e) {
            Miapi.LOGGER.error("Failed reading pack.mcmeta at {}", metaPath, e);
        }

        return fallback;
    }
}
