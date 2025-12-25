package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;

import java.util.List;
import java.util.Map;

public class ModuleRemoveDropProperty implements CraftingProperty {
    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<ResourceLocation, JsonElement> data) {
        return null;
    }
}
