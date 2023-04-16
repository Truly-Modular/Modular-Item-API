package smartin.miapi.item.modular.properties.crafting;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.ModuleProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CraftingProperty implements ModuleProperty {
    @Nullable
    public InteractAbleWidget createGui(int x, int y, int width, int height){
        return null;
    }

    public List<Vec2f> getSlotPositions(){
        return new ArrayList<>();
    }

    public void writeCraftingBuffer(PacketByteBuf buf,InteractAbleWidget createdGui){

    }

    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory,PacketByteBuf buf) {
        return true;
    }

    public abstract ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory,PacketByteBuf buf);

    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory,PacketByteBuf buf) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(this.preview(old, crafting, player, newModule, module, inventory,buf));
        stacks.addAll(inventory);
        return stacks;
    }
}