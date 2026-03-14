package smartin.miapi.modules.properties.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public interface InventoryType {

    ResourceLocation getId();

    Component getName();

    int getSize(ItemStack container);

    boolean canInsert(ItemStack container, ItemStack stack);

    double priority();

    default Slot createSlot(Container container, int index, int x, int y, SlotInfo slotInfo, Player player, ItemStack containerSource) {
        return new Slot(container, index, x, y) {
            public boolean mayPlace(ItemStack stack) {
                return canInsert(containerSource, stack);
            }
        };
    }

    default Container decode(ItemStack container, ItemContainerContents contents) {
        SimpleContainer inv = new SimpleContainer(getSize(container)) {
            @Override
            public boolean canAddItem(ItemStack stack) {
                return super.canAddItem(stack) && canInsert(container, stack);
            }
        };

        if (contents != null) {
            for (int i = 0; i < contents.stream().toList().size() && i < inv.getContainerSize(); i++) {
                inv.setItem(i, contents.stream().toList().get(i));
            }
        }

        return inv;
    }

    default ItemContainerContents encode(Container inventory) {
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            stacks.add(inventory.getItem(i));
        }

        return ItemContainerContents.fromItems(stacks);
    }
}
