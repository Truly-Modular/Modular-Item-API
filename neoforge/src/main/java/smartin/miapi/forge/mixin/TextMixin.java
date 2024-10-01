package smartin.miapi.forge.mixin;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Component.class)
public abstract class TextMixin {

    /*
    @Inject(
            method = "Lnet/minecraft/text/TranslatableTextContent;updateTranslations()V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void miapi$FakeTranslation(CallbackInfo ci) {
        String key = ((TranslatableTextContent) (Object) this).getKey();
        if (FakeTranslation.translations.containsKey(key)) {
            String translation = FakeTranslation.translations.get(key);
            ((TranslatableTextContentAccessor) this).setTranslations(ImmutableList.of(StringVisitable.plain(translation)));
            ci.cancel();
        }
    }

     */
}
