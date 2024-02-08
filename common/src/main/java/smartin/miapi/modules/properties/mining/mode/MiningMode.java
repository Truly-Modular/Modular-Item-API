package smartin.miapi.modules.properties.mining.mode;

import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.mining.modifier.MiningModifier;

import java.util.List;

/**
 * Mining Modes are supposed to control how sth is mined.
 * They control the actual mining Part
 * They are not meant to filter the mining blocks
 * {@link MiningModifier} is meant to filter blocks from the shape
 */
public interface MiningMode {
    MiningMode fromJson(JsonObject object, ModuleInstance moduleInstance);

    void execute(List<BlockPos> posList, World world, ServerPlayerEntity player, BlockPos origin, ItemStack itemStack);

    default void removeDurability(double durability, ItemStack itemStack, World world, ServerPlayerEntity player) {
        double additionalChance = durability - Math.floor(durability);
        if (additionalChance > 0) {
            if (world.random.nextDouble() > additionalChance) {
                durability++;
            }
        }
        if (Math.floor(durability) > 0) {
            itemStack.damage((int) Math.floor(durability), player, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }
    }

}
