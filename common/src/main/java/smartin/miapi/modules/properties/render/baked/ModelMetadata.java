package smartin.miapi.modules.properties.render.baked;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Deals with custom .mcmeta data
 *
 * @param colorProvider
 * @param lightValues
 * @param transform     optional transform to apply to the model
 * @param modelSources  optional list of model source paths to try as fallbacks
 */
public record ModelMetadata(String colorProvider, int[] lightValues, Transform transform, List<String> modelSources) {
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

        @Override
        public @NotNull String getMetadataSectionName() {
            return "miapi_model_data";
        }

        @Override
        public @NotNull ModelMetadata fromJson(JsonObject json) {
            String data = null;
            int[] light = null;
            Transform transform = Transform.IDENTITY;
            List<String> sources = List.of();
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
            if (json.has("transform")) {
                transform = Transform.CODEC.decode(JsonOps.INSTANCE, json.get("transform")).getOrThrow().getFirst();
            }
            if (json.has("modelSources")) {
                JsonArray sourcesArray = json.get("modelSources").getAsJsonArray();
                for (com.google.gson.JsonElement element : sourcesArray) {
                    sources.add(element.getAsString());
                }
            }
            return new ModelMetadata(data, light, transform, sources);
        }

        public static final ModelMetadata EMPTY = new ModelMetadata(null, null, Transform.IDENTITY, List.of());

    }
}
