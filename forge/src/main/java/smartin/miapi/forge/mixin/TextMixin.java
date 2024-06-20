package smartin.miapi.forge.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.registries.FakeTranslation;

@Mixin(TranslatableTextContent.class)
public abstract class TextMixin {

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
}
