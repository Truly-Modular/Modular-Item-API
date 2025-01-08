package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import smartin.miapi.forge.TrulyModularForge;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;

public class EpicFightCompat {
    public static void setup() {
        onModEventBusClient(TrulyModularForge.trulyModularEventBus);
    }

    @OnlyIn(Dist.CLIENT)
    public static void onModEventBusClient(IEventBus eventBus) {
        eventBus.<PatchedRenderersEvent.Modify>addListener((event) -> {
            PatchedEntityRenderer patt3553$temp = event.get(EntityType.PLAYER);
            if (patt3553$temp instanceof PPlayerRenderer playerrenderer) {
                playerrenderer.addCustomLayer(new CustomLivingArmorRenderer<>(null));
            }
        });
    }
}
