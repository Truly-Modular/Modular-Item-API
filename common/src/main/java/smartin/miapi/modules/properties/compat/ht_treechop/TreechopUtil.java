package smartin.miapi.modules.properties.compat.ht_treechop;

import ht.treechop.api.IChoppingItem;
import ht.treechop.api.TreeChopAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.Miapi;
import smartin.miapi.registries.RegistryInventory;

public class TreechopUtil {
    public static TreeChopAPI api = null;

    public static void setTreechopApi(Object object) {
        api = (TreeChopAPI) object;
        RegistryInventory.MODULAR_ITEMS.addCallback(item -> {
            api.registerChoppingItemBehavior(item, new IChoppingItem() {
                @Override
                public boolean canChop(Player playerEntity, ItemStack itemStack, Level world, BlockPos blockPos, BlockState blockState) {
                    Miapi.LOGGER.info("chop check "+TreechopProperty.property.getValue(itemStack).orElse(0.0).intValue());
                    return item instanceof AxeItem || TreechopProperty.property.getValue(itemStack).orElse(0.0).intValue() > 0;
                }

                @Override
                public int getNumChops(ItemStack itemStack, BlockState blockState) {

                    return TreechopProperty.property.getValue(itemStack).orElse(0.0).intValue() +
                           (item instanceof AxeItem ? 1 : 0);
                }
            });
        });
    }
}
