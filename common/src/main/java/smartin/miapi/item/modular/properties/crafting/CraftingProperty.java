package smartin.miapi.item.modular.properties.crafting;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

public abstract class CraftingProperty implements ModuleProperty {

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return false;
    }

    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, SlotProperty.ModuleSlot slot, ItemModule module){
        return true;
    }

    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, SlotProperty.ModuleSlot slot, ItemModule module){
        return crafting;
    }

    public ItemStack performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, SlotProperty.ModuleSlot slot, ItemModule module){
        return this.preview(old,crafting,player,slot,module);
    }
}
