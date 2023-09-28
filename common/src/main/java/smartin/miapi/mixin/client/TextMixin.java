package smartin.miapi.mixin.client;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.registries.FakeTranslation;

@Mixin(Text.class)
public interface TextMixin {

    @Inject(
            method = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void miapi$FakeTranslation(String key, CallbackInfoReturnable<MutableText> cir) {
        if (FakeTranslation.translations.containsKey(key)) {
            cir.setReturnValue(Text.literal(FakeTranslation.translations.get(key)));
        }
    }
}
