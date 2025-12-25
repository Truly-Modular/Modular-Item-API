package smartin.miapi.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.editor.MiapiEditor;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), cancellable = true)
    private void miapi$editorRenderCallback(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (MiapiConfig.getClientConfig().other.enableEditorMixin ) {
            MiapiEditor.renderAll(guiGraphics, deltaTracker);
        }
    }
}
