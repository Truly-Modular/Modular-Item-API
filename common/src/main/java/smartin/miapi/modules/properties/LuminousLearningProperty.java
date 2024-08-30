package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.mixin.ExperienceDroppingBlockAccessor;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * Increases Xp Drops from slain mods and broken blocks
 */
public class LuminousLearningProperty extends DoubleProperty {
    public static final String KEY = "luminiousLearning";
    public static LuminousLearningProperty property;

    public LuminousLearningProperty() {
        super(KEY);
        property = this;
        BlockEvent.BREAK.register((World level, BlockPos pos, BlockState state, ServerPlayerEntity player, @Nullable IntValue xp) -> {
            if (player.isCreative()) {
                return EventResult.pass();
            }
            if (xp == null && state.getBlock() instanceof ExperienceDroppingBlock xpBlock) {
                xp = new IntValue() {
                    @Override
                    public void accept(int value) {

                    }

                    @Override
                    public int getAsInt() {
                        return ((ExperienceDroppingBlockAccessor) xpBlock).getExperienceDropped().get(level.getRandom());
                    }
                };
            }
            if (
                    level instanceof ServerWorld serverWorld &&
                    xp != null && xp.get() > 0) {
                double value = getForItems(player.getItemsEquipped()) / 3;
                while (value > 0) {
                    if (Math.random() < value) {
                        ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(pos), xp.get());
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
        EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource source) -> {
            if (entity.getWorld() instanceof ServerWorld serverWorld && source.getAttacker() instanceof LivingEntity attacker) {
                int xp = entity.getXpToDrop();
                double value = getForItems(attacker.getItemsEquipped()) / 1.5;
                while (value > 0) {
                    if (Math.random() < value) {
                        ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(entity.getBlockPos()), xp);
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
    }


    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
