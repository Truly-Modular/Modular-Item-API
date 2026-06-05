package smartin.miapi.modules.properties.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentBackedContainer implements Container {
    private final ItemStack owner;
    private final ResourceLocation inventoryId;
    private final int size;

    public ComponentBackedContainer(
            ItemStack owner,
            ResourceLocation inventoryId,
            int size
    ) {
        this.owner = owner;
        this.inventoryId = inventoryId;
        this.size = size;
    }

    private List<ItemStack> loadItems() {
        InventoryComponent component =
                owner.getOrDefault(
                        InventoryComponent.ITEM_INVENTORIES,
                        InventoryComponent.EMPTY
                );

        InventoryComponent.CachedContents contents =
                component.inventories().getOrDefault(
                        inventoryId,
                        InventoryComponent.CachedContents.EMPTY
                );

        List<ItemStack> items = new ArrayList<>(contents.getLookupCache());

        while (items.size() < size) {
            items.add(ItemStack.EMPTY);
        }

        return items;
    }

    private void saveItems(List<ItemStack> items) {
        InventoryComponent component =
                owner.getOrDefault(
                        InventoryComponent.ITEM_INVENTORIES,
                        InventoryComponent.EMPTY
                );

        Map<ResourceLocation, InventoryComponent.CachedContents> map =
                new HashMap<>(component.inventories());

        map.put(
                inventoryId,
                new InventoryComponent.CachedContents(ItemContainerContents.fromItems(items))
        );

        owner.set(
                InventoryComponent.ITEM_INVENTORIES,
                new InventoryComponent(map)
        );
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return loadItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= size)
            return ItemStack.EMPTY;

        return loadItems().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        List<ItemStack> items = loadItems();

        ItemStack existing = items.get(slot);
        ItemStack result = existing.split(amount);

        items.set(slot, existing);
        saveItems(items);

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        List<ItemStack> items = loadItems();

        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);

        saveItems(items);

        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        List<ItemStack> items = loadItems();

        items.set(slot, stack);

        saveItems(items);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            items.add(ItemStack.EMPTY);
        }

        saveItems(items);
    }

    /**
     * Attempts to insert the entire stack into this container.
     *
     * @param stack Stack to insert.
     * @return Remaining items that could not be inserted, or ItemStack.EMPTY if fully inserted.
     */
    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<ItemStack> items = loadItems();
        ItemStack remaining = stack;

        for (int i = 0; i < size && !remaining.isEmpty(); i++) {
            ItemStack slotStack = items.get(i);

            if (slotStack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                continue;
            }

            int maxSize = Math.min(
                    slotStack.getMaxStackSize(),
                    getMaxStackSize()
            );

            int space = maxSize - slotStack.getCount();

            if (space <= 0) {
                continue;
            }

            int toMove = Math.min(space, remaining.getCount());

            slotStack.grow(toMove);
            remaining.shrink(toMove);
        }
        for (int i = 0; i < size && !remaining.isEmpty(); i++) {
            if (!items.get(i).isEmpty()) {
                continue;
            }

            int toMove = Math.min(
                    remaining.getCount(),
                    Math.min(
                            remaining.getMaxStackSize(),
                            getMaxStackSize()
                    )
            );

            ItemStack inserted = remaining.copyWithCount(toMove);

            items.set(i, inserted);
            remaining.shrink(toMove);
        }

        saveItems(items);

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }
}