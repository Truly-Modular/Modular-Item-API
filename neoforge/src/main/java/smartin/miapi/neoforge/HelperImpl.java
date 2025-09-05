package smartin.miapi.neoforge;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class HelperImpl {

    public static Component getTranslation(TagKey<?> key) {
        return Component.translatableWithFallback(getTranslationKey(key), "#" + key.location().toString());
    }

    private static String getTranslationKey(TagKey<?>  tagKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag.");
        ResourceLocation registryIdentifier = tagKey.registry().location();
        ResourceLocation tagIdentifier = tagKey.location();

        if (!registryIdentifier.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            stringBuilder.append(registryIdentifier.getNamespace())
                    .append(".");
        }

        stringBuilder.append(registryIdentifier.getPath().replace("/", "."))
                .append(".")
                .append(tagIdentifier.getNamespace())
                .append(".")
                .append(tagIdentifier.getPath().replace("/", ".").replace(":", "."));

        return stringBuilder.toString();
    }
}

