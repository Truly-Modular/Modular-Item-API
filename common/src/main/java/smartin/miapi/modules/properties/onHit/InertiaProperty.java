package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class InertiaProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("inertia_damage");
    public static InertiaProperty property;

    public InertiaProperty() {
        super(KEY);
        property = this;

        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            if (damageSource.isDirect() &&
                damageSource.getEntity() instanceof LivingEntity livingAttacker
                && target instanceof LivingEntity livingDefender &&
                !livingDefender.level().isClientSide()) {

                getData(damageSource.getWeaponItem()).ifPresent(inertia -> {
                    if (inertia.getValue() > 0) {

                        Vec3 motion = livingAttacker.getKnownMovement();
                        double speed = motion.length();

                        double multiplier = computeVelocityMultiplier(speed);

                        double finalBonus = inertia.getValue() * multiplier;
                        bonusDamage.add(finalBonus * baseDamage);
                    }
                });
            }
            return EventResult.pass();
        });
    }

    /**
     * Computes the velocity-based multiplier for inertia damage.
     * <p>
     * f(x) = (log(x + 0.1) + 1) / 2
     * where x is speed in blocks/tick.
     */
    private static double computeVelocityMultiplier(double speed) {

        double value = (Math.log(speed + 0.1) + 1.0) / 4.0;

        // Keep it positive and bounded
        return Math.max(0.0, Math.min(value, 1.0));
    }
}
