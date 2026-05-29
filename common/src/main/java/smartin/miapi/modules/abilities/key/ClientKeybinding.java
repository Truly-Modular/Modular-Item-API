package smartin.miapi.modules.abilities.key;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.mixin.client.MinecraftAccessor;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClientKeybinding {
    public static boolean isUsing = false;
    public static PrioritizedEvent<ClientKeyBindPressEvent> CLIENT_KEY_PRESS_EVENT = PrioritizedEvent.createEventResult();
    public static PrioritizedEvent<ClientKeyBindReleaseEvent> CLIENT_KEY_PRESS_END_EVENT = PrioritizedEvent.createEventResult();

    public interface ClientKeyBindPressEvent {
        EventResult process(Minecraft minecraft, LocalPlayer player, MiapiBinding binding, List<InteractionHand> hands, AtomicReference<Boolean> requireModularItem, AtomicReference<Boolean> requireItemAbility);
    }

    public interface ClientKeyBindReleaseEvent {
        EventResult process(Minecraft minecraft, LocalPlayer player, MiapiBinding binding);
    }

    public static void clientTick(Minecraft client) {
        LocalPlayer player = client.player;
        ItemAbilityManager.clientKeyBindID.remove(player);
        Collection<MiapiBinding> bindings = KeyBindManager.BINDING_REGISTRY.getFlatMap().values();
        if (player != null) {
            for (MiapiBinding binding : bindings) {
                if (!binding.asKeyMapping().isDown() && binding.lastDown) {
                    client.gameMode.releaseUsingItem(player);
                    isUsing = false;
                    binding.lastDown = binding.asKeyMapping().isDown();
                    removeActiveButton(client, binding, player);
                    return;
                }
            }
            if (player.isUsingItem()) {
                for (MiapiBinding binding : bindings) {
                    if (binding.lastDown) {
                        while (binding.asKeyMapping().consumeClick()) {
                            ItemAbilityManager.clientKeyBindID.put(player, binding.id);
                        }
                    }
                }
            } else {
                for (MiapiBinding binding : bindings) {
                    while (binding.asKeyMapping().consumeClick()) {
                        //stat use item logic
                        startItemUseLogic(binding, player);
                    }
                }
            }
            if (((MinecraftAccessor) client).getRightClickDelay() == 0 && !player.isUsingItem()) {
                for (MiapiBinding binding : bindings) {
                    if (binding.asKeyMapping().isDown()) {
                        startItemUseLogic(binding, player);
                    }
                }
            }
        }
    }

    private static void startItemUseLogic(MiapiBinding binding, LocalPlayer player) {
        //start use item logic here
        if (startUseItem(Minecraft.getInstance(), binding, player)) {
            isUsing = true;
        }
        binding.lastDown = true;
    }

    private static void removeActiveButton(Minecraft client, MiapiBinding binding, LocalPlayer player) {
        if (CLIENT_KEY_PRESS_END_EVENT.invoker().process(client, player, binding).interruptsFurtherEvaluation()) {
            return;
        }
        KeyBindManager.updateServerId(Miapi.id("none"), client.player);
        ItemAbilityManager.clientKeyBindID.remove(player);
    }

    private static boolean startUseItem(Minecraft minecraft, MiapiBinding binding, LocalPlayer player) {
        if (!minecraft.gameMode.isDestroying()) {
            ((MinecraftAccessor) minecraft).setRightClickDelay(4);
            if (!minecraft.player.isHandsBusy()) {
                if (minecraft.hitResult == null) {
                    Miapi.LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                }
                List<InteractionHand> hands = new ArrayList<>(List.of(binding.hands));
                AtomicReference<Boolean> requireModularItem = new AtomicReference(true);
                AtomicReference<Boolean> requireItemAbility = new AtomicReference(Boolean.TRUE);

                if (CLIENT_KEY_PRESS_EVENT.invoker().process(minecraft, player, binding, hands, requireModularItem, requireItemAbility).interruptsFurtherEvaluation()) {
                    return false;
                }

                for (InteractionHand interactionHand : hands) {
                    ItemStack itemStack = minecraft.player.getItemInHand(interactionHand);
                    if (ModularItem.isModularItem(itemStack) || !requireModularItem.get()) {
                        if (!itemStack.isItemEnabled(minecraft.level.enabledFeatures())) {
                            return false;
                        }
                        if (KeyBindAbilityManagerProperty.property.getData(itemStack).isEmpty() && requireItemAbility.get()) {
                            //full prevent execution if there is no ability. reduces networking
                            return false;
                        }
                        ItemAbilityManager.clientKeyBindID.put(minecraft.player, binding.id);
                        KeyBindManager.updateServerId(binding.id, minecraft.player);

                        if (minecraft.hitResult != null) {
                            switch (minecraft.hitResult.getType()) {
                                case ENTITY:
                                    if (binding.entityInteraction ) {
                                        EntityHitResult entityHitResult = (EntityHitResult) minecraft.hitResult;
                                        Entity entity = entityHitResult.getEntity();
                                        if (!minecraft.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                                            return false;
                                        }

                                        InteractionResult interactionResult = minecraft.gameMode.interactAt(minecraft.player, entity, entityHitResult, interactionHand);
                                        if (!interactionResult.consumesAction()) {
                                            interactionResult = minecraft.gameMode.interact(minecraft.player, entity, interactionHand);
                                        }

                                        if (interactionResult.consumesAction()) {
                                            if (interactionResult.shouldSwing()) {
                                                minecraft.player.swing(interactionHand);
                                            }

                                            return true;
                                        }
                                        return false;
                                    }
                                    break;
                                case BLOCK:
                                    if (binding.blockInteraction) {
                                        BlockHitResult blockHitResult = (BlockHitResult) minecraft.hitResult;
                                        int i = itemStack.getCount();
                                        InteractionResult interactionResult2 = minecraft.gameMode.useItemOn(minecraft.player, interactionHand, blockHitResult);
                                        if (interactionResult2.consumesAction()) {
                                            if (interactionResult2.shouldSwing()) {
                                                minecraft.player.swing(interactionHand);
                                                if (!itemStack.isEmpty() && (itemStack.getCount() != i || minecraft.gameMode.hasInfiniteItems())) {
                                                    minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                                                }
                                            }

                                            return true;
                                        }

                                        if (interactionResult2 == InteractionResult.FAIL) {
                                            return false;
                                        }
                                    }
                            }
                        }

                        if (!itemStack.isEmpty() && (binding.itemInteraction)) {
                            InteractionResult interactionResult3 = minecraft.gameMode.useItem(minecraft.player, interactionHand);
                            if (interactionResult3.consumesAction()) {
                                if (interactionResult3.shouldSwing()) {
                                    minecraft.player.swing(interactionHand);
                                }

                                minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
