package smartin.miapi.modules.properties.mining.mode;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.MiningShapeProperty;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;

import java.util.List;

/**
 * Mining Modes are supposed to control how sth is mined.
 * They control the actual mining Part
 * They are not meant to filter the mining blocks
 * {@link MiningModifier} is meant to filter blocks from the shape
 */
public interface MiningMode {

    void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack);

    default void removeDurability(double durability, ItemStack itemStack, Level world, ServerPlayer player) {
        double additionalChance = durability - Math.floor(durability);
        if (additionalChance > 0) {
            if (world.random.nextDouble() > additionalChance) {
                durability++;
            }
        }
        if (Math.floor(durability) > 0) {
            itemStack.hurtAndBreak((int) Math.floor(durability), player, EquipmentSlot.MAINHAND);
        }
    }

    ResourceLocation getID();

    default boolean tryBreakBlock(ServerPlayer player, BlockPos pos) {
        MiningShapeProperty.blockedPositions.add(pos);
        boolean didBreak = player.gameMode.destroyBlock(pos);
        MiningShapeProperty.blockedPositions.remove(pos);
        return didBreak;
    }

}
