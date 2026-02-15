package smartin.miapi.modules.properties.render.baked;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.io.IOException;
import java.util.Optional;

/**
 * Deals with custom .mcmeta data
 * @param colorProvider
 * @param lightValues
 */
public record ModelMetadata(String colorProvider, int[] lightValues) {
    public static ModelMetadata fromPath(ResourceLocation identifier) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(identifier);
            if (resource.isPresent()) {
                return resource.get().metadata().getSection(new ModelDecoder()).orElse(ModelDecoder.EMPTY);
            }
        } catch (IOException ignored) {
        }
        return ModelDecoder.EMPTY;
    }

    public static class ModelDecoder implements MetadataSectionSerializer<ModelMetadata> {

        public static final ModelMetadata EMPTY = new ModelMetadata(null, null);

        @Override
        public @NotNull String getMetadataSectionName() {
            return "miapi_model_data";
        }

        @Override
        public @NotNull ModelMetadata fromJson(JsonObject json) {
            String data = null;
            int[] light = null;
            if (json.has("modelProvider")) {
                data = json.get("modelProvider").getAsString();
                if (!ColorProvider.colorProviders.containsKey(data)) {
                    Miapi.LOGGER.error("Color Provider " + data + " does not exist");
                    data = null;
                }
            }
            if (json.has("lightValues")) {
                EmissivityProperty.LightJson lightJson = EmissivityProperty.property.decode(json.get("lightValues"));
                light = lightJson.asArray();
            }
            return new ModelMetadata(data, light);
        }
    }
}
