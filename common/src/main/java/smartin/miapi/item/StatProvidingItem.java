package smartin.miapi.item;

import com.mojang.datafixers.util.Function4;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.craft.stat.CraftingStat;

import java.util.List;

/**
 * StatProvidingItems provide {@link CraftingStat}s. Use this when {@link smartin.miapi.modules.properties.StatProvisionProperty} does not meet your needs.
 * (Most of the time you'll be using this for non-modular items.)
 */
public class StatProvidingItem extends Item {
    private final Function4<ModularWorkBenchEntity, List<ItemStack>, PlayerEntity, ItemStack, CraftingStat.StatMap<?>> statGetter;

    public StatProvidingItem(Settings settings, Function4<ModularWorkBenchEntity, List<ItemStack>, PlayerEntity, ItemStack, CraftingStat.StatMap<?>> statGetter) {
        super(settings);
        this.statGetter = statGetter;
    }

    /**
     * A method used to get the crafting stat -> stat instance map.
     * You MUST return a fully wildcarded map, otherwise severe issues may arise.
     *
     * @param bench             the modular workbench block entity these stats are being sent to
     * @param providedInventory the inventory/items currently being scanned. In most cases, this is not different to {@link net.minecraft.entity.player.PlayerInventory#main}
     * @param player            the player indirectly providing these stats (often the holder of the inventory)
     * @param stack             the itemstack currently being read for crafting stat details
     * @return the map of crafting stats -> stat instances that will be provided to the workbench block entity.
     */
    public CraftingStat.StatMap<?> getStats(ModularWorkBenchEntity bench, List<ItemStack> providedInventory, PlayerEntity player, ItemStack stack) {
        return statGetter.apply(bench, providedInventory, player, stack);
    }
}
