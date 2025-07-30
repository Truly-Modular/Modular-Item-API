package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import smartin.miapi.forge.TrulyModularForge;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;

public class EpicFightCompat {
    public static void setup() {
        TrulyModularForge.BUS.register(new EpicFightCompat());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModEventBusClient(PatchedRenderersEvent.Modify event) {
        PatchedEntityRenderer patt3553$temp = event.get(EntityType.PLAYER);
        if (patt3553$temp instanceof PPlayerRenderer playerrenderer) {
            playerrenderer.addCustomLayer(new CustomModularArmorRenderer<>(null));
        }
    }
}
