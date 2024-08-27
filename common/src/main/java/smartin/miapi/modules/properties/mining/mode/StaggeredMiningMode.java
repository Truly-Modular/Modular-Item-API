package smartin.miapi.modules.properties.mining.mode;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StaggeredMiningMode implements MiningMode {
    public static List<Runnable> nextTickTask = new ArrayList<>();
    public static MapCodec<StaggeredMiningMode> CODEC = AutoCodec.of(StaggeredMiningMode.class);
    public static ResourceLocation ID = Miapi.id("staggered");

    static {
        TickEvent.SERVER_POST.register((server -> {
            List<Runnable> currentTicks = new ArrayList<>(nextTickTask);
            nextTickTask.clear();
            currentTicks.forEach(server::execute);
        }));
    }

    @CodecBehavior.Optional
    public float speed = 1.0f;
    @CodecBehavior.Optional
    @AutoCodec.Name("durability_break_chance")
    public double durabilityBreakChance = 1.0;

    @Override
    public void execute(List<BlockPos> posList, Level world, ServerPlayer player, BlockPos origin, ItemStack itemStack) {
        List<BlockPos> reducedList = new ArrayList<>(posList);
        reducedList.sort(Comparator.comparingDouble((pos) -> pos.distSqr(origin)));
        nextTickTask.add(() -> {
            BlockPos pos;
            int success = 0;
            do {
                pos = reducedList.remove(0);
                if (tryBreakBlock(player, pos)) {
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

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
