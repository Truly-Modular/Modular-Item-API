package smartin.miapi.craft;

import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.SlotProperty;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import java.util.ArrayList;
import java.util.List;

public class Crafter {
    public static ItemStack craft(ItemStack old, SlotProperty.ModuleSlot slot, ItemModule toAdd){
        ItemStack replaceStack = old.copy();
        replaceStack.getNbt().remove(ModularItemCache.cacheKey);
        if (slot == null) {
            //remove/replace base Module
            if (toAdd == null) {
                replaceStack.getNbt().putString("modules", "{module:}");
            } else {
                replaceStack.getNbt().putString("modules", "{module:" + toAdd.getName() + "}");
            }
        } else {
            ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(old);
            ItemModule.ModuleInstance instance = slot.parent;
            ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
            List<Integer> location = new ArrayList<>();
            while (instance.parent != null) {
                int slotNumber = SlotProperty.getSlotNumberIn(instance);
                location.add(slotNumber);
                Miapi.LOGGER.error(String.valueOf(slotNumber));
                instance = instance.parent;
            }
            ItemModule.ModuleInstance parsingInstance = newBaseModule;
            for (int i = location.size() - 1; i >= 0; i--) {
                Miapi.LOGGER.warn(String.valueOf(location.get(i)));
                Miapi.LOGGER.error(newBaseModule.toString());
                parsingInstance = parsingInstance.subModules.get(location.get(i));
            }
            if (toAdd == null) {
                parsingInstance.subModules.remove(slot.id);
            } else {
                parsingInstance.subModules.put(slot.id, new ItemModule.ModuleInstance(toAdd));
                toAdd.getProperties().forEach((propertyId,propertyData)->{
                    if(Miapi.modulePropertyRegistry.get(propertyId) instanceof CraftingProperty craftingProperty){
                        //Crafting Logic
                    }
                });
            }
            replaceStack.getNbt().putString("modules", newBaseModule.toString());
        }
        return replaceStack;
    }
}
