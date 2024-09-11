package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property allows additional damage based on a percentage of the target's current health.
 *
 * @header Health Percent Damage Property
 * @path /data_types/properties/on_hit/health_percent
 * @description_start
 * The Health Percent Damage Property adds extra damage to a target based on the percentage of their current health. This means that the lower the target's health, the more damage they will receive from the attack.
 * This property is useful for creating weapons or effects that scale their damage relative to the health of the target, making them more effective against low-health enemies.
 * The additional damage is calculated as a percentage of the target's current health, providing a dynamic scaling effect during combat.
 * It is recommended to use this sparingly, since against modded bosses this might escalate fairly quickly!
 * @description_end
 * @data value: The percentage of the target's current health that is added as extra damage. For example, a value of 10 means an additional 10% of the target's current health is added to the damage dealt.
 */

public class HealthPercentDamage extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("health_percent");
    public static HealthPercentDamage property;

    public HealthPercentDamage() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent -> {
            if (livingHurtEvent.damageSource.getEntity() instanceof LivingEntity livingAttacker) {
                ItemStack itemStack = livingHurtEvent.getCausingItemStack();
                if (livingAttacker instanceof Player player) {
                    if (player.oAttackAnim != 0.0) {
                        return EventResult.pass();
                    }
                }
                double percentage = getValue(itemStack).orElse(0.0);
                double increasingBy = livingHurtEvent.livingEntity.getHealth() / (100 / percentage);
                livingHurtEvent.amount += increasingBy;
            }
            return EventResult.pass();
        }));
    }
}
