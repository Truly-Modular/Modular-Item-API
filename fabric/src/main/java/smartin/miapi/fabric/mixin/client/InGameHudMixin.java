package smartin.miapi.fabric.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.events.ClientEvents;

@Mixin(Gui.class)
public class InGameHudMixin {
    private static final ResourceLocation ICONS = ResourceLocation.parse("textures/gui/icons.png");

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void miapi$customDrawContext(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEvents.HUD_RENDER.invoker().render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }

    @Inject(
            method = "renderHotbarAndDecorations",
            at = @At("TAIL")
    )
    private void miapi$customDrawContext2(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        /*
        Gui inGameHud = (Gui) (Object) (this);
        Player playerEntity = ((InGameHudAccessor) inGameHud).callGetCameraPlayer();
        int heartBars = ((InGameHudAccessor) inGameHud).callGetHeartRows(((InGameHudAccessor) inGameHud).callGetHeartCount(playerEntity));
        ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(playerEntity);
        // Calculate health and absorption values
        int playerHealth = MathHelper.ceil(playerEntity.getHealth());
        int renderHealthValue = ((InGameHudAccessor) inGameHud).getRenderHealthValue();
        int scaledWidth = ((InGameHudAccessor) inGameHud).getScaledWidth();
        int shieldingArmorMaxAmount = (int) facet.getMaxAmount() / 2;
        int shieldingArmorCurrentAmount = (int) (facet.getCurrentAmount());
        int scaledHeight = ((InGameHudAccessor) inGameHud).getScaledHeight();
        float maxHealth = Math.max((float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float) Math.max(renderHealthValue, playerHealth));
        int absorptionAmount = MathHelper.ceil(playerEntity.getAbsorptionAmount());
        int healthAbsorptionTotal = MathHelper.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
        int numHearts = Math.max(10 - (healthAbsorptionTotal - 2), 3);
        int startY = scaledHeight - 39 - 10;
        if (MiapiConfig.INSTANCE.client.shieldingArmor.respectHealth) {
            startY -= (healthAbsorptionTotal - 1) * numHearts;
        }
        if (MiapiConfig.INSTANCE.client.shieldingArmor.respectArmor && playerEntity.getArmor() > 0) {
            startY -= 10;
        }
        startY -= MiapiConfig.INSTANCE.client.shieldingArmor.otherOffests * 10;
        startY -= MiapiConfig.INSTANCE.client.shieldingArmor.attributesSingleLine.stream()
                          .filter(id -> Registries.ATTRIBUTE.containsId(id))
                          .map(id -> Registries.ATTRIBUTE.get(id))
                          .filter(entityAttribute -> playerEntity.getAttributes().hasAttribute(entityAttribute))
                          .filter(entityAttribute -> playerEntity.getAttributeValue(entityAttribute) > 1)
                          .count() * 10;
        for (
                int index = 0;
                index < ( (float) facet.getMaxAmount() ) / 2.0f; index++) {
            int heartX = scaledWidth / 2 - 91 + (index % 10) * 8;
            int yOffset = (index / 10) * 10;
            int heartTextureIndex = index * 2 + 1;
            if (heartTextureIndex < shieldingArmorCurrentAmount) {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 430, 96, 9, 9, 512, 512);
            } else if (heartTextureIndex == shieldingArmorCurrentAmount) {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 439, 96, 9, 9, 512, 512);
            } else {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 448, 96, 9, 9, 512, 512);
            }
        }
         */
    }
}
