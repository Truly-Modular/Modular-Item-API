package smartin.miapi.modules.abilities.key;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.mixin.MinecraftAccessor;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;

import java.util.Collection;

public class ClientKeybinding {
    public static boolean isUsing = false;

    public static void clientTick(Minecraft client) {
        LocalPlayer player = client.player;
        ItemAbilityManager.clientKeyBindID.remove(player);
        Collection<MiapiBinding> bindings = KeyBindManager.BINDING_REGISTRY.getFlatMap().values();
        if (player != null) {
            //Miapi.LOGGER.info("is using " + player.isUsingItem());
            if (player.isUsingItem()) {
                //Miapi.LOGGER.info("using item");
                for (MiapiBinding binding : bindings) {
                    if (!binding.asKeyMapping().isDown() && binding.lastDown) {
                        client.gameMode.releaseUsingItem(player);
                        isUsing = false;
                        binding.lastDown = binding.asKeyMapping().isDown();
                        return;
                    }
                }
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
                        ResourceLocation id = ItemAbilityManager.clientKeyBindID.get(player);
                        ItemAbilityManager.clientKeyBindID.put(player, binding.id);
                        if (startUseItem(Minecraft.getInstance(), binding)) {
                            binding.lastDown = true;
                            isUsing = true;
                        } else {
                            ItemAbilityManager.clientKeyBindID.put(player, id);
                        }
                    }
                }
            }
            if (((MinecraftAccessor) client).getRightClickDelay() == 0 && !player.isUsingItem()) {
                for (MiapiBinding binding : bindings) {
                    if (binding.asKeyMapping().isDown()) {
                        //start use item logic here
                        ResourceLocation id = ItemAbilityManager.clientKeyBindID.get(player);
                        ItemAbilityManager.clientKeyBindID.put(player, binding.id);
                        if (startUseItem(Minecraft.getInstance(), binding)) {
                            binding.lastDown = true;
                            isUsing = true;
                        } else {
                            ItemAbilityManager.clientKeyBindID.put(player, id);
                        }
                    }
                }
            }
        }
    }

    private static boolean startUseItem(Minecraft minecraft, MiapiBinding binding) {
        if (!minecraft.gameMode.isDestroying()) {
            ((MinecraftAccessor) minecraft).setRightClickDelay(4);
            if (!minecraft.player.isHandsBusy()) {
                if (minecraft.hitResult == null) {
                    Miapi.LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                }

                for (InteractionHand interactionHand : binding.hands) {
                    ItemStack itemStack = minecraft.player.getItemInHand(interactionHand);
                    if (ModularItem.isModularItem(itemStack)) {
                        if (!itemStack.isItemEnabled(minecraft.level.enabledFeatures())) {
                            return false;
                        }
                        if (KeyBindAbilityManagerProperty.property.getData(itemStack).isEmpty()) {
                            //full prevent execution if there is no ability. reduces networking
                            return false;
                        }
                        KeyBindManager.updateServerId(binding.id, minecraft.player);

                        if (minecraft.hitResult != null) {
                            switch (minecraft.hitResult.getType()) {
                                case ENTITY:
                                    if (binding.entityInteraction) {
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

                        if (!itemStack.isEmpty() && binding.itemInteraction) {
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
