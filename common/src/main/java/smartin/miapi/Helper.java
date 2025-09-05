package smartin.miapi;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;

public class Helper {

    @ExpectPlatform
    public static Component getTranslation(TagKey<?> tagKey) {
        throw new UnsupportedOperationException("need to be implemented on each platform");
    }
}
