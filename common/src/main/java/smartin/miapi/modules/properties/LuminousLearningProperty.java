package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Optional;

/**
 * Increases Xp Drops from slain mods and broken blocks
 *
 * @header Luminous Learning Property
 * @path /data_types/properties/luminous_learning
 * @description_start The LuminousLearningProperty enhances the experience drops from both blocks and entities. When an item with this property
 * is used to break blocks or when a player slays a mob, the amount of XP dropped is increased based on the configured value.
 * @description_end
 * @data luminious_learning: the amount of XP increase.
 */

public class LuminousLearningProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("luminious_learning");
    public static LuminousLearningProperty property;

    public LuminousLearningProperty() {
        super(KEY);
        property = this;
        EntityEvent.LIVING_DEATH.register((LivingEntity entity, DamageSource source) -> {
            if (entity.level() instanceof ServerLevel serverWorld && source.getEntity() instanceof LivingEntity attacker) {
                int xp = entity.getExperienceReward(serverWorld, source.getEntity());
                double value = (
                                       getForItems(attacker.getAllSlots())
                               ) / 2.5;
                while (value > 0) {
                    if (Math.random() < value) {
                        ExperienceOrb.award(serverWorld, Vec3.atCenterOf(entity.blockPosition()), xp);
                    }
                    value--;
                }
            }
            return EventResult.pass();
        });
    }

    public int getAdjustedXp(int original, ServerLevel level, ItemStack stack, int experience) {
        int adjusted = original;
        if (stack != null && ModularItem.isModularItem(stack)) {
            Optional<Double> optional = getValue(stack);
            if (optional.isPresent()) {
                int value = optional.get().intValue();
                int bonusRolls = 0;

                while (value > 0) {
                    if (Math.random() > 0.7) {
                        bonusRolls++;
                    }
                    value--;
                }

                adjusted += bonusRolls * experience;
            }
        }

        return adjusted;
    }

}
