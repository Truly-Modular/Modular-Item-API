package smartin.miapi.forge.compat;

import dev.architectury.event.EventResult;
import dev.shadowsoffire.apotheosis.ench.enchantments.masterwork.CrescendoEnchant;
import net.minecraft.item.CrossbowItem;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiProjectileEvents;

public class ApotheosisCompat {
    public static void setup() {
        MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.register((player, crossbow)-> {
            try {
                if (crossbow.getItem() instanceof CrossbowItem) {
                    CrescendoEnchant.onArrowFired(crossbow);
                }
            } catch (Exception surpressed) {
                Miapi.LOGGER.warn("apotheosis compat error", surpressed);
            }
            return EventResult.pass();
        });

        MiapiProjectileEvents.MODULAR_BOW_POST_SHOT.register(event -> {
            try {
                if (event.bowStack.getItem() instanceof CrossbowItem) {
                    CrescendoEnchant.markGeneratedArrows(event.projectile, event.bowStack);
                }
            } catch (Exception surpressed) {
                Miapi.LOGGER.warn("apotheosis compat error", surpressed);
            }
            return EventResult.pass();
        });

        MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_SHOT.register((player, crossbow) -> {
            try {
                CrescendoEnchant.preArrowFired(crossbow);
            } catch (Exception surpressed) {
                Miapi.LOGGER.warn("apotheosis compat error", surpressed);
            }
            return EventResult.pass();
        });
    }
}
