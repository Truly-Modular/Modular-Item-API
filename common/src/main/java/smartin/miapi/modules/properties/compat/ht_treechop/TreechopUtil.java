package smartin.miapi.modules.properties.compat.ht_treechop;

import ht.treechop.api.IChoppingItem;
import ht.treechop.api.TreeChopAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.registries.RegistryInventory;

public class TreechopUtil {
    public static TreeChopAPI api = null;

    public static void setTreechopApi(Object object){
        api = (TreeChopAPI) object;
        api.registerChoppingItemBehavior(RegistryInventory.modularAxe, new IChoppingItem() {
            @Override
            public boolean canChop(Player playerEntity, ItemStack itemStack, Level world, BlockPos blockPos, BlockState blockState) {
                return true;
            }

            @Override
            public int getNumChops(ItemStack itemStack, BlockState blockState) {
                return TreechopProperty.property.getValue(itemStack).orElse(0.0).intValue() + 1;
            }
        });

        api.registerChoppingItemBehavior(RegistryInventory.modularMattock, new IChoppingItem() {
            @Override
            public boolean canChop(Player playerEntity, ItemStack itemStack, Level world, BlockPos blockPos, BlockState blockState) {
                return true;
            }

            @Override
            public int getNumChops(ItemStack itemStack, BlockState blockState) {
                return TreechopProperty.property.getValue(itemStack).orElse(0.0).intValue() + 1;
            }
        });
    }
}
