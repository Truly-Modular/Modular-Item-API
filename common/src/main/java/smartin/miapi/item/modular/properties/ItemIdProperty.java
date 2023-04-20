package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import java.util.List;

public class ItemIdProperty extends CraftingProperty {
    public static final String key = "itemId";
    public static ModuleProperty itemIdProperty;

    public ItemIdProperty(){
        itemIdProperty = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        assert  Miapi.itemRegistry.get(data.getAsString()) != null;
        return true;
    }

    @Override
    public float getPriority() {
        return 0;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        ItemModule.ModuleInstance root = ModularItem.getModules(crafting);
        String translationKey = "";
        for(ItemModule.ModuleInstance moduleInstance: root.allSubModules()){
            JsonElement data = moduleInstance.getProperties().get(itemIdProperty);
            if(data!=null){
                translationKey = data.getAsString();
            }
        }
        Item item = Miapi.itemRegistry.get(translationKey);
        if(item!=null){
            ItemStack newStack = new ItemStack(item);
            newStack.setNbt(crafting.getNbt());
            return newStack;
        }
        return crafting;
    }
}
