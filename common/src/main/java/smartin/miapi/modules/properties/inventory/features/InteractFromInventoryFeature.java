package smartin.miapi.modules.properties.inventory.features;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.key.ClientKeybinding;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InteractFromInventoryFeature implements InventoryFeatureType<ResourceLocation> {
    public static final InteractFromInventoryFeature FEATURE = new InteractFromInventoryFeature();
    public static final FacetKey<SimpleEntityFacet<List<ItemStack>>> USED_ITEM_STORAGE = SimpleEntityFacet
            .createSimple(Miapi.id("player_using_stored_items"), Codec.list(ItemStack.CODEC))
            .syncToClientsOnSet(false)
            .setSaveCondition(l -> !l.isEmpty())
            .build(List.of());

    public static final FacetKey<SimpleEntityFacet.NullAbleEntityFacet<ItemAndSlot>> USED_ITEM_AND_SLOT = SimpleEntityFacet
            .createSimple(Miapi.id("player_using_inventory_and_slot"), AutoCodec.of(ItemAndSlot.class).codec())
            .syncToClientsOnSet(false)
            .setSaveCondition(l -> false)
            .buildNullable();

    public static ModernNetworking.ClientToServerManager<ResourceLocation> SWAP_BUTTON_PRESSED = new ModernNetworking.ClientToServerManager<>(
            Miapi.id("swap_to_backpack_c2s"),
            Miapi.ID_CODEC,
            (data, player, registry) -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    trySwappingWithBackpack(serverPlayer, data);
                }
            });

    public static ModernNetworking.ServerToClientManager<ResourceLocation> CURRENT_SWAPPED_ITEM = new ModernNetworking.ServerToClientManager<>(
            Miapi.id("swap_to_backpack_s2c"),
            Miapi.ID_CODEC,
            (data, player, registry) -> {

            });

    public static record ItemAndSlot(ItemStack stack, ResourceLocation id, int slot, boolean cleanNextTick) {
    }


    private InteractFromInventoryFeature() {
        if (Platform.getEnv() == EnvType.CLIENT) {
            clientSetup();
        }
    }

    public static void trySwappingWithBackpack(ServerPlayer player, ResourceLocation backpackID) {
        Optional<Pair<InventoryInstance, Container>> potentialNonEmptyContainer =
                ItemInventoryManager.getInventoriesWith(
                                player,
                                InteractFromInventoryFeature.FEATURE,
                                invID -> invID.equals(backpackID)
                        )
                        .map(instance -> Pair.of(instance, instance.create()))
                        .filter(pair -> !pair.getSecond().isEmpty())
                        .findFirst();
        if (potentialNonEmptyContainer.isPresent()) {
            Miapi.LOGGER.info("start using item");
            Pair<InventoryInstance, Container> pair = potentialNonEmptyContainer.get();

            InventoryInstance instance = pair.getFirst();
            Container container = pair.getSecond();

            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i).copy();

                if (!stack.isEmpty()) {
                    ItemStack oldMainhandItem = player.getMainHandItem();
                    USED_ITEM_AND_SLOT.get(player).set(new ItemAndSlot(instance.getOwningItem(), instance.getType().getId(), i, false));

                    if (!oldMainhandItem.isEmpty()) {
                        List<ItemStack> oldItems = USED_ITEM_STORAGE.get(player).get();
                        List<ItemStack> newItems = new ArrayList<>(oldItems);
                        newItems.add(oldMainhandItem);
                        Miapi.LOGGER.info("storing old hotbar item: " + oldMainhandItem.getDisplayName().getString());
                        USED_ITEM_STORAGE.get(player).set(newItems);
                    }
                    Miapi.LOGGER.info("setting new hotbar item: " + stack.getDisplayName().getString());
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);

                    container.removeItem(i, oldMainhandItem.getCount());
                    instance.save(container);
                    CURRENT_SWAPPED_ITEM.sendToClientPlayer(player, backpackID);
                    return;
                }
            }
        }
        returnOldPlayerHandItems(player);
        CURRENT_SWAPPED_ITEM.sendToClientPlayer(player, Miapi.id("none"));
    }

    private static void releaseAndReturnToBackPack(Player player, ItemAndSlot itemAndSlot) {
        ItemStack usedMainhandItem = player.getMainHandItem();
        ItemInventoryManager.getAllInventories(player).stream().filter(inventoryInstance ->
                inventoryInstance.getOwningItem().equals(itemAndSlot.stack) &&
                inventoryInstance.getType().getId().equals(itemAndSlot.id)
        ).findFirst().ifPresent(inventoryInstance -> {
            Container container = inventoryInstance.create();
            if (container.canPlaceItem(itemAndSlot.slot(), usedMainhandItem)) {
                ItemStack incoming = usedMainhandItem.copy();
                Miapi.LOGGER.info("returning to backpack " + incoming.getDisplayName().getString());
                ItemStack existing = container.getItem(itemAndSlot.slot());
                ItemStack remainder = tryMerge(container, itemAndSlot.slot(), incoming);

                if (!remainder.isEmpty()) {
                    remainder = tryInsertAnywhere(container, remainder);
                }
                USED_ITEM_AND_SLOT.get(player).set(null);
                inventoryInstance.save(container);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                returnOldPlayerHandItems(player);
                if (!remainder.isEmpty()) {
                    player.addItem(remainder);
                }
            }
        });
    }

    private static boolean isUsingSave(Player player) {
        return player.isUsingItem() || !(player.getUseItem() != null && player.getUseItem().isEmpty());
    }

    private static ItemStack tryMerge(Container container, int slot, ItemStack stack) {
        ItemStack existing = container.getItem(slot);

        if (existing.isEmpty()) {
            container.setItem(slot, stack);
            return ItemStack.EMPTY;
        }

        if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int max = Math.min(existing.getMaxStackSize(), container.getMaxStackSize());

            int canMove = Math.min(stack.getCount(), max - existing.getCount());

            if (canMove > 0) {
                existing.grow(canMove);
                container.setItem(slot, existing);
                stack.shrink(canMove);
            }
        }

        return stack;
    }

    private static ItemStack tryInsertAnywhere(Container container, ItemStack stack) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (stack.isEmpty()) break;
            stack = tryMerge(container, i, stack);
        }
        return stack;
    }

    public static ResourceLocation CLIENT_CURRENT_SWAPPED_BACKPACK = Miapi.id("none");

    @Environment(EnvType.CLIENT)
    private void clientSetup() {
        ClientKeybinding.CLIENT_KEY_PRESS_END_EVENT.register(new ClientKeybinding.ClientKeyBindReleaseEvent() {
            @Override
            public EventResult process(Minecraft minecraft, LocalPlayer player, MiapiBinding binding) {
                //send msg to release items to server
                return EventResult.pass();
            }
        });
        ClientKeybinding.CLIENT_KEY_PRESS_EVENT.register(new ClientKeybinding.ClientKeyBindPressEvent() {
            //Client side put item in hand for prediction
            @Override
            public EventResult process(Minecraft minecraft, LocalPlayer player, MiapiBinding binding, List<InteractionHand> hands, AtomicReference<Boolean> requireModularItem, AtomicReference<Boolean> requireItemAbility) {
                Optional<Pair<InventoryInstance, Container>> potentialNonEmptyContainer =
                        ItemInventoryManager.getInventoriesWith(
                                        player,
                                        InteractFromInventoryFeature.FEATURE,
                                        invID -> invID.equals(binding.id)
                                )
                                .map(instance -> Pair.of(instance, instance.create()))
                                .filter(pair -> !pair.getSecond().isEmpty())
                                .findFirst();
                if (potentialNonEmptyContainer.isPresent()) {
                    ResourceLocation featureID = potentialNonEmptyContainer.get().getFirst().getFeatures().get(InteractFromInventoryFeature.FEATURE).orElse(Miapi.id("none"));
                    if (!binding.lastDown) {
                        SWAP_BUTTON_PRESSED.sendServer(featureID, player.registryAccess());
                    }
                    if (potentialNonEmptyContainer.get().getFirst().getFeatures().get(InteractFromInventoryFeature.FEATURE).map(id -> id.equals(CLIENT_CURRENT_SWAPPED_BACKPACK)).orElse(false)) {
                        requireItemAbility.set(false);
                        requireModularItem.set(false);
                        return EventResult.pass();
                    }
                    return EventResult.interruptDefault();
                }
                SWAP_BUTTON_PRESSED.sendServer(Miapi.id("none"), player.registryAccess());
                return EventResult.pass();
            }
        });
    }

    private static void returnOldPlayerHandItems(Player player) {
        if (!player.isUsingItem()) {
            SimpleEntityFacet<List<ItemStack>> facet = USED_ITEM_STORAGE.get(player);
            if (facet != null) {
                List<ItemStack> items = facet.get();
                for (ItemStack itemStack : items) {
                    Miapi.LOGGER.info("returning to player hotbar " + itemStack.getDisplayName().getString());
                    if (!itemStack.isEmpty()) {
                        if (player.getMainHandItem().isEmpty()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                        } else {
                            player.addItem(itemStack);
                        }
                    }
                }
                facet.set(List.of());
            }
        }
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public ResourceLocation id() {
        return Miapi.id("interact_inventory_item_binding");
    }

    @Override
    public Codec<ResourceLocation> codec() {
        return Miapi.ID_CODEC;
    }

    @Override
    public ResourceLocation initialize(ResourceLocation property, ModuleInstance context) {
        return property;
    }
}