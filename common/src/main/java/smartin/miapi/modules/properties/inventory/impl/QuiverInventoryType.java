package smartin.miapi.modules.properties.inventory.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.inventory.InventoryType;

public class QuiverInventoryType implements InventoryType {

    private static final ResourceLocation ID = Miapi.id("quiver");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getName() {
        return Component.literal("Quiver");
    }

    @Override
    public int getSize(ItemStack container) {
        if (container.getItem() instanceof Equipable equipable && equipable.getEquipmentSlot() == EquipmentSlot.CHEST) {
            return 3;
        }
        return 0;
    }


    @Override
    public double priority(){
        return -1;
    }

    @Override
    public boolean canInsert(ItemStack container, ItemStack stack) {
        return stack.getItem() instanceof ArrowItem;
    }
}