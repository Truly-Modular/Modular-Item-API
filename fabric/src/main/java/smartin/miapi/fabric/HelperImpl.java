package smartin.miapi.fabric;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;

public class HelperImpl {

    public static Component getTranslation(TagKey<?> tagKey) {
        return tagKey.getName();
    }
}
