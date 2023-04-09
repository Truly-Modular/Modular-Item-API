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
        Miapi.LOGGER.warn(old.getNbt().toString());
        ItemStack craftingStack = old.copy();
        if(!old.hasNbt() || !old.getNbt().contains("modules")){
            Miapi.LOGGER.error("old Item has no Modules - something went very wrong");
            return old;
        }
        //remove CacheKey so new cache gets Generated
        craftingStack.getNbt().remove(ModularItemCache.cacheKey);

        if (toAdd == null && (slot==null || slot.parent==null)) {
            //base slot remove should return air
            return ItemStack.EMPTY;
        }
        ItemModule.ModuleInstance oldBaseModule = ModularItem.getModules(old);
        ItemModule.ModuleInstance instance = slot.parent;
        ItemModule.ModuleInstance newBaseModule = ItemModule.ModuleInstance.fromString(oldBaseModule.toString());
        Miapi.LOGGER.warn(oldBaseModule.toString());
        if(instance==null){
            //a module already exists, replacing module 0
            craftingStack.getNbt().putString("modules", generateNew(toAdd).toString());
            return craftingStack;
        }
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
            Miapi.LOGGER.warn(String.valueOf(parsingInstance.module.getName()));
            parsingInstance = parsingInstance.subModules.get(location.get(i));
        }
        if (toAdd == null) {
            parsingInstance.subModules.remove(slot.id);
        } else {
            parsingInstance.subModules.put(slot.id, generateNew(toAdd));
        }
        craftingStack.getNbt().putString("modules", newBaseModule.toString());
        return craftingStack;
    }

    private static ItemModule.ModuleInstance generateNew(ItemModule module){
        ItemModule.ModuleInstance generated = new ItemModule.ModuleInstance(module);
        module.getProperties().forEach((propertyId,propertyData)->{
            if(Miapi.modulePropertyRegistry.get(propertyId) instanceof CraftingProperty craftingProperty){
                //Crafting Logic
            }
        });
        return generated;
    }
}
