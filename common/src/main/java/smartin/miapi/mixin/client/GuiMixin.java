package smartin.miapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
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

    @Inject(method = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"), cancellable = true)
    private void miapi$editorRenderCallback(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (MiapiConfig.INSTANCE.client.other.enableEditorMixin) {
            RenderSystem.enableDepthTest();
            MiapiEditor.renderAll(guiGraphics, deltaTracker);
            RenderSystem.disableDepthTest();
        }
    }
}
