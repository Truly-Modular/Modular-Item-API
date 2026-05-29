package smartin.miapi.modules.properties.inventory;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import smartin.miapi.modules.properties.inventory.features.InventoryFeatureType;
import smartin.miapi.modules.properties.inventory.features.InventorySizeFeatureType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class InventoryInstance {
    @AutoCodec.Ignored
    InventoryFeatureType.FeatureSet features;
    @AutoCodec.Ignored
    ItemStack owningItem;
    @AutoCodec.Ignored
    InventoryType type;
    @AutoCodec.Ignored
    Player owningPlayer;
    @AutoCodec.Ignored
    SlotInfo slot;

    public InventoryInstance(Player player, ItemStack itemStack, InventoryType inventoryType,SlotInfo slot) {
        this.owningPlayer = player;
        this.owningItem = itemStack;
        this.type = inventoryType;
        this.slot = slot;
        AtomicReference<InventoryFeatureType.FeatureSet> set = new AtomicReference<>(new InventoryFeatureType.FeatureSet(inventoryType.getDefaultFeatures().all()));
        ItemInventoryManager.GET_INVENTORY_FEATURES_EVENT.invoker().getFeatures(set, player, itemStack, inventoryType);
        features = set.get();
    }

    public InventoryFeatureType.FeatureSet getFeatures() {
        return features;
    }

    public ItemStack getOwningItem() {
        return owningItem;
    }

    public SlotInfo getSlot(){
        return slot;
    }

    public int getSize() {
        return features.get(InventorySizeFeatureType.FEATURE).map(r -> (int) r.getValue()).orElse(0);
    }

    public boolean canInsert(ItemStack stack) {
        AtomicBoolean atomicBoolean = new AtomicBoolean(true);
        features.forEach((inventoryFeature, data) -> {
            if (!inventoryFeature.allows(stack, data)) {
                atomicBoolean.set(false);
            }
        });
        return atomicBoolean.get();
    }

    public Container create() {
        SimpleContainer inv = new SimpleContainer(getSize()) {
            @Override
            public boolean canAddItem(ItemStack stack) {
                return super.canAddItem(stack) && canInsert(stack);
            }
        };
        InventoryComponent component = owningItem.getOrDefault(InventoryComponent.ITEM_INVENTORIES, new InventoryComponent(Map.of()));
        ItemContainerContents contents =
                component.inventories().getOrDefault(type.getId(), ItemContainerContents.EMPTY);

        if (contents != null) {
            for (int i = 0; i < contents.stream().toList().size() && i < inv.getContainerSize(); i++) {
                inv.setItem(i, contents.stream().toList().get(i));
            }
        }

        return inv;
    }

    public void save(Container container) {
        InventoryComponent component = owningItem.get(InventoryComponent.ITEM_INVENTORIES);

        Map<ResourceLocation, ItemContainerContents> map =
                component != null
                        ? new HashMap<>(component.inventories())
                        : new HashMap<>();

        map.put(type.getId(), encode(container));

        owningItem.set(InventoryComponent.ITEM_INVENTORIES, new InventoryComponent(map));
    }

    private static ItemContainerContents encode(Container inventory) {
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            stacks.add(inventory.getItem(i));
        }

        return ItemContainerContents.fromItems(stacks);
    }

    public Slot createSlot(Container container, int index, int x, int y, SlotInfo slotInfo, Player player, ItemStack containerSource) {
        return new Slot(container, index, x, y) {
            public boolean mayPlace(ItemStack stack) {
                return canInsert(stack);
            }
        };
    }

    public InventoryType getType() {
        return type;
    }
}
