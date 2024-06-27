package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class RarityProperty implements CraftingProperty, ModuleProperty {
    public static String KEY = "rarity";
    public static RarityProperty property;

    public RarityProperty() {
        property = this;
    }

    public static Rarity getRarity(ItemStack itemStack) {
        try {
            JsonElement jsonElement = ItemModule.getMergedProperty(itemStack, property);
            if (jsonElement != null) {
                return applyEnchant(itemStack, fromString(jsonElement.getAsString()));
            }
        } catch (Exception ignored) {

        }
        return applyEnchant(itemStack, Rarity.COMMON);
    }


    private static Rarity applyEnchant(ItemStack itemStack, Rarity old) {
        if (!itemStack.isEnchanted()) {
            return old;
        } else {
            switch (old) {
                case COMMON:
                case UNCOMMON:
                    return Rarity.RARE;
                case RARE:
                    return Rarity.EPIC;
                case EPIC:
                default:
                    return old;
            }
        }
    }

    private static Rarity fromString(String string) {
        for (Rarity rarity : Rarity.values()) {
            if (rarity.toString().equalsIgnoreCase(string)) {
                return rarity;
            }
        }
        return Rarity.COMMON;
    }


    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, Player player, ModularWorkBenchEntity bench, CraftAction craftAction, ItemModule module, List<ItemStack> inventory, Map<String, String> data) {
        ModularItemCache.clearUUIDFor(crafting);
        Rarity rarity = getRarity(crafting);
        crafting.set(DataComponents.RARITY, rarity);
        return crafting;
    }
}
