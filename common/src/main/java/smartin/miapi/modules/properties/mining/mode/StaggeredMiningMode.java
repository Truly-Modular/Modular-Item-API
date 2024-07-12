package smartin.miapi.modules.properties.mining.mode;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record StaggeredMiningMode(float speed, double durabilityBreakChance) implements MiningMode {
    public static List<Runnable> nextTickTask = new ArrayList<>();

    static {
        TickEvent.SERVER_POST.register((server -> {
            List<Runnable> currentTicks = new ArrayList<>(nextTickTask);
            nextTickTask.clear();
            currentTicks.forEach(server::execute);
        }));
    }

    public static Codec<StaggeredMiningMode> CODEC = RecordCodecBuilder.create((miningModeInstance -> {
        return miningModeInstance.group(
                        Codec.FLOAT
                                .fieldOf("speed")
                                .forGetter(StaggeredMiningMode::speed),
                        Codec.DOUBLE
                                .fieldOf("durability_chance")
                                .forGetter(StaggeredMiningMode::durabilityBreakChance)
                )
                .apply(miningModeInstance, StaggeredMiningMode::new);
    }));

    @Override
    public void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.distSqr(origin)));
        nextTickTask.add(() -> {
            BlockPos pos;
            int success = 0;
            do {
                pos = reducedList.remove(0);
                if (world.destroyBlock(pos, MiningLevelProperty.mineBlock(itemStack, world, world.getBlockState(pos), pos, player) && !player.isCreative(), player))
                {
                    success++;
                    if (!player.isCreative()) {
                        removeDurability(durabilityBreakChance, itemStack, world, player);
                    }
                }
            } while (
                    success < speed
                    && !reducedList.isEmpty() && itemStack.getMaxDamage() - itemStack.getDamageValue() > 1
            );
            if (!reducedList.isEmpty() && itemStack.getMaxDamage() - itemStack.getDamageValue() > 1) {
                execute(reducedList, world, player, origin, itemStack);
            }
        });
    }
}
