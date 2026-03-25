package smartin.miapi.registries;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import smartin.miapi.Miapi;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
