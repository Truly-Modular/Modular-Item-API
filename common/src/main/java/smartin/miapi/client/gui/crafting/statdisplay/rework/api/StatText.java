package smartin.miapi.client.gui.crafting.statdisplay.rework.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface StatText<T, M> {
    Component get(ItemStack stack, T value, M metaData);
}