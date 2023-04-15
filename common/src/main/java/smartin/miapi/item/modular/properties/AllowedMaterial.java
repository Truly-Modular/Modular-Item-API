package smartin.miapi.item.modular.properties;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import java.util.List;

public class AllowedMaterial extends CraftingProperty {
    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return crafting;
    }
}
