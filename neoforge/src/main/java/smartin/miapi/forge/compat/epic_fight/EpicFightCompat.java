package smartin.miapi.forge.compat.epic_fight;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import smartin.miapi.Miapi;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = Miapi.MOD_ID)
public class EpicFightCompat {
    public static void setup() {
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onModEventBusClient(PatchedRenderersEvent.Modify event) {
        PatchedEntityRenderer patt3553$temp = event.get(EntityType.PLAYER);
        if (patt3553$temp instanceof PPlayerRenderer playerrenderer) {
            playerrenderer.addCustomLayer(new CustomModularArmorRenderer<>(null));
        }
    }
}
