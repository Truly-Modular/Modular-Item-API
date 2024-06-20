package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
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
        BlockEvent.BREAK.register((World level, BlockPos pos, BlockState state, ServerPlayerEntity player, @Nullable IntValue xp)->{
            ItemStack tool = player.getMainHandStack();
            if (tool != null && tool.getItem() instanceof ModularItem) {
                double value = getValueSafe(tool);
                while (value > 0) {
                    if (Math.random() > 0.7 && xp!=null && level instanceof ServerWorld serverWorld) {
                        ExperienceOrbEntity.spawn(serverWorld, Vec3d.ofCenter(pos), xp.get());
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
        EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource source) -> {
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                int xp = entity.getXpToDrop();
                double value = getForItems(entity.getItemsEquipped());
                while (value > 0) {
                    if (Math.random() > 0.7) {
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
