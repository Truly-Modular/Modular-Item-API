package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * @header Executioner Property
 * @path /data_types/properties/on_hit/executioner
 * @description_start
 * The ExecutionerProperty increases melee damage against targets below 50% health.
 *
 * Each point of Executioner grants 1% bonus damage while the target is below
 * 50% of its maximum health.
 * @description_end
 * @data executioner: A double value indicating the executioner damage bonus.
 */
public class ExecutionerProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("executioner");
    public static ExecutionerProperty property = new ExecutionerProperty();

    public ExecutionerProperty() {
        super(KEY);
        MiapiEvents.MODIFY_DAMAGE_EVENT.register(
                (target, itemStack, baseDamage, damageSource, bonusDamage,level) -> {
                    if (target instanceof LivingEntity livingEntity) {
                        double executioner = getValue(itemStack).orElse(0.0);

                        if (executioner > 0
                            && livingEntity.getHealth() < livingEntity.getMaxHealth() * 0.5) {
                            float bonus = (float) (baseDamage * (executioner / 100.0));
                            bonusDamage.add(bonus);
                        }
                    }
                }
        );
    }
}
