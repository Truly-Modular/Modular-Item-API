package smartin.miapi.modules.properties.compat.ht_treechop;

import ht.treechop.api.IChoppingItem;
import ht.treechop.api.TreeChopAPI;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.registries.RegistryInventory;

public class TreechopUtil {

    public static void setTreechopApi(Object object){
        api = (TreeChopAPI) object;
    }

    public static TreeChopAPI api = null;

    static void setup() {

        api.registerChoppingItemBehavior(RegistryInventory.modularAxe, new IChoppingItem() {
            @Override
            public boolean canChop(PlayerEntity playerEntity, ItemStack itemStack, World world, BlockPos blockPos, BlockState blockState) {
                return true;
            }

            @Override
            public int getNumChops(ItemStack itemStack, BlockState blockState) {
                return (int) TreechopProperty.property.getValueSafe(itemStack) + 1;
            }
        });

        api.registerChoppingItemBehavior(RegistryInventory.modularMattock, new IChoppingItem() {
            @Override
            public boolean canChop(PlayerEntity playerEntity, ItemStack itemStack, World world, BlockPos blockPos, BlockState blockState) {
                return true;
            }

            @Override
            public int getNumChops(ItemStack itemStack, BlockState blockState) {
                return (int) TreechopProperty.property.getValueSafe(itemStack) + 1;
            }
        });
    }
}
