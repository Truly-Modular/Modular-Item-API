package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
        BlockEvent.BREAK.register((Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp)->{
            ItemStack tool = player.getMainHandItem();
            if (tool != null && tool.getItem() instanceof ModularItem) {
                getValue(tool).ifPresent((value)->{
                    while (value > 0) {
                        if (Math.random() > 0.7 && xp!=null && level instanceof ServerLevel serverWorld) {
                            ExperienceOrb.award(serverWorld, Vec3.atCenterOf(pos), xp.get());
                        }
                        value--;
                    }
                });
            }
            return EventResult.pass();
        });
        //TODO: create a common adjust XP event
        EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource source) -> {
            if (entity.level() instanceof ServerLevel serverWorld) {
                int xp = entity.getBaseExperienceReward();
                double value = getForItems(entity.getAllSlots());
                while (value > 0) {
                    if (Math.random() > 0.7) {
                        ExperienceOrb.award(serverWorld, Vec3.atCenterOf(entity.blockPosition()), xp);
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
    }
}
