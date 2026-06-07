package smartin.miapi.modules.abilities.key.handler.backpack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.client.MinecraftAccessor;
import smartin.miapi.modules.abilities.key.handler.UseItemAbilityHandler;
import smartin.miapi.modules.properties.inventory.screen.preview.InventoryPreviewManager;

import java.util.List;

public final class UseFromBackpackClient {

    private static boolean SESSION_ACTIVE;
    private static boolean SESSION_ACTIVE_WITH_ITEM;
    private static int slotId = 0;
    private static ResourceLocation CURRENT_INVENTORY = Miapi.id("none");

    private UseFromBackpackClient() {
    }

    public static void onPress(UseFromBackpackHandler handler, Minecraft mc, LocalPlayer player) {
        if (SESSION_ACTIVE) return;
        slotId = player.getInventory().selected;

        UseFromBackpackHandler.REQUEST_START_USE.sendServer(
                handler.openId,
                player.registryAccess()
        );
    }

    public static void onHeld(UseFromBackpackHandler handler, Minecraft mc, LocalPlayer player) {
        if (!SESSION_ACTIVE) return;

        if (player.getInventory().selected != slotId) {
            abort(handler, player);
            return;
        }
        if (player.getMainHandItem().has(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT)) {
            if (!SESSION_ACTIVE_WITH_ITEM) {
                SESSION_ACTIVE_WITH_ITEM = true;
            }
        } else {
            if (SESSION_ACTIVE_WITH_ITEM) {
                abort(handler, player);
            }
        }

        InventoryPreviewManager.show(CURRENT_INVENTORY);
        if (((MinecraftAccessor) mc).getMiapiRightClickDelay() == 0
            && !player.isUsingItem()) {

            UseItemAbilityHandler.isUsing =
                    UseItemAbilityHandler.tryUse(
                            mc,
                            player,
                            null,
                            List.of(InteractionHand.MAIN_HAND),
                            handler.entityInteraction,
                            handler.blockInteraction,
                            handler.itemInteraction
                    );
        }
    }

    public static void onRelease(UseFromBackpackHandler handler, Minecraft mc, LocalPlayer player) {
        abort(handler, player);
    }

    public static void handleStartAck(ResourceLocation id) {
        Minecraft mc = Minecraft.getInstance();
        CURRENT_INVENTORY = id;
        LocalPlayer player = mc.player;
        if (player == null) return;

        SESSION_ACTIVE = true;

        UseItemAbilityHandler.isUsing =
                UseItemAbilityHandler.tryUse(
                        mc,
                        player,
                        null,
                        List.of(InteractionHand.MAIN_HAND),
                        true,
                        true,
                        true
                );
    }

    public static void handleAbortAck(boolean abort) {
        SESSION_ACTIVE = false;
        SESSION_ACTIVE_WITH_ITEM = false;

        UseItemAbilityHandler.isUsing = false;
    }

    private static void abort(UseFromBackpackHandler handler, LocalPlayer player) {
        if (!SESSION_ACTIVE) return;

        UseFromBackpackHandler.REQUEST_ABORT_USE.sendServer(
                slotId,
                player.registryAccess()
        );

        SESSION_ACTIVE = false;
        UseItemAbilityHandler.isUsing = false;
    }
}