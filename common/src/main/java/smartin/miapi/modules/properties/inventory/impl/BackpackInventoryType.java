package smartin.miapi.modules.properties.inventory.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.inventory.InventoryType;

public class BackpackInventoryType implements InventoryType {

    private static final ResourceLocation ID = Miapi.id("backpack");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getName() {
        return Component.literal("Backpack");
    }

    @Override
    public int getSize(ItemStack container) {
        return 7;
    }

    @Override
    public double priority(){
        return 0;
    }

    @Override
    public boolean canInsert(ItemStack container, ItemStack stack) {
        return true;
    }
}