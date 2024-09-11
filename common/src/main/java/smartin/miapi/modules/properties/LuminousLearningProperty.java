package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * Increases Xp Drops from slain mods and broken blocks
 * @header Luminous Learning Property
 * @path /data_types/properties/luminous_learning
 * @description_start
 * The LuminousLearningProperty enhances the experience drops from both blocks and entities. When an item with this property
 * is used to break blocks or when a player slays a mob, the amount of XP dropped is increased based on the configured value.
 *
 * @description_end
 * @data luminious_learning: the amount of XP increase.
 */

public class LuminousLearningProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("luminious_learning");
    public static LuminousLearningProperty property;

    public LuminousLearningProperty() {
        super(KEY);
        property = this;
        //TODO:this needs reworking
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
        MiapiEvents.ADJUST_DROP_XP.register((entity,xp)->{
            if (entity.level() instanceof ServerLevel serverWorld) {
                double value = getForItems(entity.getAllSlots());
                while (value > 0) {
                    if (Math.random() > 0.7) {
                        xp.add(xp.getValue());
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
    }
}
