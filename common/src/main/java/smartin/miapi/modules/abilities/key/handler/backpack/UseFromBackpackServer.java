package smartin.miapi.modules.abilities.key.handler.backpack;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.entity.SimpleEntityFacet;
import dev.architectury.event.EventResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.properties.inventory.InventoryComponent;
import smartin.miapi.modules.properties.inventory.InventoryInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.inventory.features.GrowToFillStateFeatureType;
import smartin.miapi.modules.properties.inventory.features.InteractFromInventoryFeature;
import smartin.miapi.modules.properties.inventory.screen.preview.InventoryPreviewManager;

import java.util.*;

public final class UseFromBackpackServer {

    public static final FacetKey<SimpleEntityFacet<List<ItemStack>>> USED_ITEM_STORAGE =
            SimpleEntityFacet.createSimple(
                            Miapi.id("player_using_stored_items"),
                            Codec.list(ItemStack.CODEC)
                    )
                    .setPredicate(e -> true)
                    .syncToClientsOnSet(false)
                    .setSaveCondition(l -> !l.isEmpty())
                    .build(List.of());

    public static record UseFromBackpackData(ResourceLocation inventoryID, ResourceLocation slotId, int slot,
                                             long age) {
        public static Codec<UseFromBackpackData> CODEC = AutoCodec.of(UseFromBackpackData.class).codec();
    }

    private static final Map<UUID, PendingRestore> PENDING_RESTORES =
            new HashMap<>();

    private record PendingRestore(
            int slot,
            int delayTicks
    ) {
    }

    static {
        MiapiEvents.PLAYER_TICK_END.register(new MiapiEvents.PlayerTickEvent() {
            @Override
            public EventResult tick(Player player) {
                if (player instanceof ServerPlayer serverPlayer) {
                    tickPlayer(serverPlayer);
                }
                return EventResult.pass();
            }
        });
    }

    public static void onStartUseRequest(ResourceLocation requested, Player player, RegistryAccess access) {
        if (!(player instanceof ServerPlayer sp)) return;

        // locate matching backpack inventory
        Optional<InventoryInstance> maybeInstance =
                ItemInventoryManager.getInventoriesWith(
                        player,
                        InteractFromInventoryFeature.FEATURE,
                        id -> id.contains(requested)
                ).findFirst();

        if (maybeInstance.isEmpty()) return;

        InventoryInstance instance = maybeInstance.get();
        Container container = instance.create();

        int foundSlot = -1;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (!stack.isEmpty()) {
                foundSlot = i;
                break;
            }
        }

        if (foundSlot == -1) return;

        InventoryPreviewManager.PREVIEW.sendToClientPlayer(sp, instance.getType().getId());

        ItemStack oldMain = player.getMainHandItem().copy();

        if (!oldMain.isEmpty()) {
            storeRecoveryItem(instance.getOwningItem(), oldMain);
        }

        ItemStack toUse = container.getItem(foundSlot).copy();
        toUse.set(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT, new UseFromBackpackData(instance.getType().id, instance.getSlot().getID(), foundSlot, 20));

        player.setItemInHand(InteractionHand.MAIN_HAND, toUse);


        container.removeItem(foundSlot, toUse.getCount());
        instance.save(container);

        UseFromBackpackHandler.START_USE_ACK.sendToClientPlayer(sp, instance.getType().id);
    }

    public static void tickPlayer(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.has(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT)) {
            if (!player.isUsingItem()) {
                if (mainHand.get(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT).age < 0) {
                    mainHand.remove(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT);
                    return;
                }
                mainHand.update(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT,
                        new UseFromBackpackData(KeyBindManager.NONE, KeyBindManager.NONE, 0, 100),
                        old -> {
                            return new UseFromBackpackData(old.inventoryID(), old.slotId(), old.slot(), old.age() - 1);
                        });
            }
        }
        PendingRestore pending =
                PENDING_RESTORES.get(player.getUUID());

        if (pending != null) {
            if (pending.delayTicks() <= 0) {
                PENDING_RESTORES.remove(player.getUUID());
                resetItemToBackpack(player, pending.slot());
            } else {
                PENDING_RESTORES.put(
                        player.getUUID(),
                        new PendingRestore(
                                pending.slot(),
                                pending.delayTicks() - 1
                        )
                );
            }
        }
    }

    public static void onAbortUseRequest(
            Integer selected,
            Player player,
            RegistryAccess access
    ) {
        PENDING_RESTORES.put(
                player.getUUID(),
                new PendingRestore(selected, 1)
        );
    }

    private static void resetItemToBackpack(Player player, int slotId) {
        if (!(player instanceof ServerPlayer sp)) return;

        ItemStack lastUsed = moveItemToBackPack(player, slotId);
        returnStoredItemsToPlayer(player, slotId);

        UseFromBackpackHandler.ABORT_USE_ACK.sendToClientPlayer(sp, true);
    }

    private static void returnStoredItemsToPlayer(Player player, int slotId) {
        ItemInventoryManager.getAllInventories(player).forEach(inventoryInstance -> {
            List<ItemStack> stored = new ArrayList<>(consumeRecoveryItems(inventoryInstance.getOwningItem()));
            if (!stored.isEmpty() && player.getInventory().getItem(slotId).isEmpty()) {
                ItemStack first = stored.removeFirst();
                player.getInventory().setItem(slotId, first);
            }

            for (ItemStack stack : stored) {
                if (!player.addItem(stack)) {
                    player.drop(stack, false);
                }
            }
        });
    }

    private static @NotNull ItemStack moveItemToBackPack(Player player, int slotId) {
        ItemStack lastUsed = player.getInventory().getItem(slotId);
        if (!lastUsed.isEmpty()) {
            UseFromBackpackData data = lastUsed.get(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT);
            lastUsed.remove(UseFromBackpackHandler.CURRENTLY_FROM_BACKPACK_COMPONENT);

            ItemStack returnStack = lastUsed.copy();

            if (data != null) {
                ItemInventoryManager.getInventoriesWith(
                        player,
                        InteractFromInventoryFeature.FEATURE,
                        id -> id.contains(data.inventoryID())
                ).filter(
                        inventoryInstance -> inventoryInstance.getType().getId().equals(data.inventoryID()) &&
                                             inventoryInstance.getSlot().getID().equals(data.slotId())
                ).findFirst().ifPresent(instance -> {
                    Container container = instance.create();
                    int slot = data.slot();

                    if (container.getItem(slot).isEmpty()) {
                        container.setItem(slot, returnStack);
                        player.getInventory().setItem(slotId, ItemStack.EMPTY);
                        instance.save(container);
                        return;
                    }

                    for (int i = 0; i < container.getContainerSize(); i++) {
                        if (container.getItem(i).isEmpty()) {
                            container.setItem(i, returnStack);
                            player.getInventory().setItem(slotId, ItemStack.EMPTY);
                            instance.save(container);
                            return;
                        }
                    }
                });
            }
        }
        return lastUsed;
    }

    public static boolean storeRecoveryItem(
            ItemStack backpack,
            ItemStack stack
    ) {
        InventoryComponent component =
                backpack.getOrDefault(
                        InventoryComponent.ITEM_INVENTORIES,
                        InventoryComponent.EMPTY
                );

        Map<ResourceLocation, InventoryComponent.CachedContents> inventories =
                new HashMap<>(component.inventories());

        InventoryComponent.CachedContents existing =
                inventories.getOrDefault(
                        GrowToFillStateFeatureType.QUICK_STORAGE_INV,
                        InventoryComponent.CachedContents.EMPTY
                );

        List<ItemStack> items =
                new ArrayList<>(existing.getLookupCache());

        items.add(stack.copy());

        inventories.put(
                GrowToFillStateFeatureType.QUICK_STORAGE_INV,
                new InventoryComponent.CachedContents(
                        ItemContainerContents.fromItems(items)
                )
        );

        backpack.set(
                InventoryComponent.ITEM_INVENTORIES,
                new InventoryComponent(inventories)
        );

        return true;
    }

    public static List<ItemStack> consumeRecoveryItems(
            ItemStack backpack
    ) {
        InventoryComponent component = backpack.get(InventoryComponent.ITEM_INVENTORIES);
        if (component == null) {
            return List.of();
        }
        Map<ResourceLocation, InventoryComponent.CachedContents> inventories =
                new HashMap<>(component.inventories());


        InventoryComponent.CachedContents stored =
                inventories.remove(
                        GrowToFillStateFeatureType.QUICK_STORAGE_INV
                );
        if (stored == null) {
            return List.of();
        }

        List<ItemStack> contents = stored.getLookupCache();

        backpack.set(
                InventoryComponent.ITEM_INVENTORIES,
                new InventoryComponent(inventories)
        );
        return contents;
    }
}