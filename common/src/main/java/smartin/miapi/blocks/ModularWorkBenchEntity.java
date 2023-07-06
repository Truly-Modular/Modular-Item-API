package smartin.miapi.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ModularWorkBenchEntity extends BlockEntity {
    private DefaultedList<ItemStack> inventory;

    public ModularWorkBenchEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
        this.inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        saveInventory(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        loadInventory(tag);
    }

    private void saveInventory(NbtCompound tag) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                NbtCompound itemTag = new NbtCompound();
                stack.writeNbt(itemTag);
                tag.put("Item_" + i, itemTag);
            }
        }
    }

    private void loadInventory(NbtCompound tag) {
        for (int i = 0; i < inventory.size(); i++) {
            String key = "Item_" + i;
            if (tag.contains(key)) {
                inventory.set(i, ItemStack.fromNbt(tag.getCompound(key)));
            } else {
                inventory.set(i, ItemStack.EMPTY);
            }
        }
    }
}