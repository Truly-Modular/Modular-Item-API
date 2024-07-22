package smartin.miapi.fabric.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.registries.FakeTranslation;

@Mixin(Component.class)
public interface TextMixin {

    @Inject(
            method = "translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void miapi$FakeTranslation(String key, CallbackInfoReturnable<MutableComponent> cir) {
        if (FakeTranslation.translations.containsKey(key)) {
            cir.setReturnValue(Component.literal(FakeTranslation.translations.get(key)));
        }
    }
}
