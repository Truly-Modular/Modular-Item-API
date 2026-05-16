package smartin.miapi.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.events.ClientEvents;

@Mixin(Gui.class)
public class InGameHudMixin {

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void miapi$customDrawContext(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEvents.HUD_RENDER.invoker().render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }

    @Inject(
            method = "renderItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;enableBlend()V"
            )
    )
    private void miapi$optimisationBatchRenderingHotbarstart(
            GuiGraphics graphics,
            DeltaTracker delta,
            CallbackInfo ci
    ) {
        if (MiapiConfig.getClientConfig().render.batch.enableHotBar) {
            MiapiItemModel.startItemBatch();
        }
    }

    @Inject(
            method = "renderItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"
            )
    )
    private void miapi$optimisationBatchRenderingHotbarend(
            GuiGraphics graphics,
            DeltaTracker delta,
            CallbackInfo ci
    ) {
        if (MiapiConfig.getClientConfig().render.batch.enableHotBar) {
            MiapiItemModel.finishBatch(graphics.bufferSource());
        }
    }

    @Inject(
            method = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V",
            at = @At("TAIL")
    )
    private static void miapi$customDrawContext2(GuiGraphics context, Player player, int y, int heartRows, int height, int x, CallbackInfo ci) {
        ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(player);
        if (facet == null) {
            return;
        }

        int startY = y - (heartRows - 1) * height - 20;
        startY -= MiapiConfig.getClientConfig().shieldingArmor.otherOffsets * 10;
        RenderSystem.enableBlend();
        for (
                int index = 0;
                index < facet.getMaxAmount() / 2.0f; index++) {
            int yOffset = (index / 10) * 10;
            int xOffset = (index % 10) * 8;
            int heartTextureIndex = index * 2 + 1;
            if (heartTextureIndex < facet.getCurrentAmount()) {
                context.blit(CraftingScreen.BACKGROUND_TEXTURE, x + xOffset, startY - yOffset, 430, 96, 9, 9, 512, 512);
            } else if (heartTextureIndex == facet.getCurrentAmount()) {
                context.blit(CraftingScreen.BACKGROUND_TEXTURE, x + xOffset, startY - yOffset, 439, 96, 9, 9, 512, 512);
            } else {
                context.blit(CraftingScreen.BACKGROUND_TEXTURE, x + xOffset, startY - yOffset, 448, 96, 9, 9, 512, 512);
            }
        }
        RenderSystem.disableBlend();
    }
}
