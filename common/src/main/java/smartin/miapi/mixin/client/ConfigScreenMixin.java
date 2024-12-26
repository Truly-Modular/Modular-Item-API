package smartin.miapi.mixin.client;

import com.redpxnda.nucleus.config.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConfigScreen.class)
public class ConfigScreenMixin {

    @Inject(
            method = "Lcom/redpxnda/nucleus/config/screen/ConfigScreen;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "TAIL"
            )
    )
    void miapi$injectUnblurTitle(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var font = Minecraft.getInstance().font;

        context.drawString(font, ((ConfigScreen) (Object) this).getTitle(), 8, 16 - font.lineHeight / 2, -11184811, true);
    }
}
