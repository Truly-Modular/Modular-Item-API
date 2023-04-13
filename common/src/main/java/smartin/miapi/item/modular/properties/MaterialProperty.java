package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import java.util.HashMap;
import java.util.List;

public class MaterialProperty extends CraftingProperty {

    public static String key = "material";

    public MaterialProperty(){
        PropertyResolver.propertyProviderRegistry.register("material",(moduleInstance -> {
            return new HashMap<>();
        }));
    }
    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory) {
        return null;
    }
}
