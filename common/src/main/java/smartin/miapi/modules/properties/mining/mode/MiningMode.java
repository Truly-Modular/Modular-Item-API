package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Mining Modes are supposed to control how sth is mined.
 * They control the actual mining Part
 * They are not meant to filter the mining blocks
 * {@link MiningModifier} is meant to filter blocks from the shape
 */
public interface MiningMode {
    MiningMode fromJson(JsonObject object, ModuleInstance moduleInstance);

    void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack);

    default void removeDurability(double durability, ItemStack itemStack, Level world, ServerPlayer player) {
        double additionalChance = durability - Math.floor(durability);
        if (additionalChance > 0) {
            if (world.random.nextDouble() > additionalChance) {
                durability++;
            }
        }
        if (Math.floor(durability) > 0) {
            itemStack.hurtAndBreak((int) Math.floor(durability), player, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
    }

}
