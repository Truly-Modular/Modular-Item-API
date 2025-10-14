package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class ComboProperty extends DoubleProperty {
    public static ResourceLocation KEY = Miapi.id("combo");
    public static ComboProperty property;

    public ComboProperty() {
        super(KEY);
        property = this;
        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            if (damageSource.isDirect() && damageSource.getEntity() instanceof LivingEntity livingAttacker && target instanceof LivingEntity livingDefender) {
                getData(damageSource.getWeaponItem()).ifPresent(combo -> {
                    int maxCombo = (int) Math.round(combo.getValue());
                    ComboFacet facet = ComboFacet.KEY.get(livingAttacker);
                    if (maxCombo > 0 && facet != null) {
                        facet.setTicksUntilReset(ComboTimeProperty.property.getTicks(damageSource.getWeaponItem()));
                        if (facet.getComboCount() > 0) {
                            spawnParticles(livingAttacker, livingDefender, (float) facet.getComboCount() / (float) maxCombo);
                        }
                        //5% damage boost per combo step
                        bonusDamage.add(baseDamage * ((float) facet.getComboCount() / 20));
                        facet.registerHit(livingDefender, maxCombo);
                    }
                });
            }
            return EventResult.pass();
        });
        MiapiEvents.LIVING_ENTITY_TICK_END.register(entity -> {
            ComboFacet facet = ComboFacet.KEY.get(entity);
            if (facet != null) {
                facet.tick();
            }
            return EventResult.pass();
        });
    }

    private static void spawnParticles(LivingEntity livingAttacker, LivingEntity livingDefender, float comboPercent) {
        if (!livingDefender.level().isClientSide()) {
            ServerLevel level = (ServerLevel) livingDefender.level();

            // --- Settings ---
            Vec3 attackerLook = livingAttacker.getLookAngle(); // attacker's facing direction (unit-ish)
            Vec3 forward = attackerLook.normalize(); // normal pointing from attacker view
            Vec3 globalUp = new Vec3(0.0, 1.0, 0.0);

            // Build right & up basis vectors for the plane that faces the attacker:
            // right = forward x globalUp (perpendicular to forward and global up)
            Vec3 right = forward.cross(globalUp);
            if (right.lengthSqr() < 1e-6) {
                // fallback if forward is nearly vertical
                right = new Vec3(1.0, 0.0, 0.0);
            } else {
                right = right.normalize();
            }
            // upInPlane is a vector in the forward-facing plane that points "up" as seen from attacker
            Vec3 upInPlane = right.cross(forward).normalize();

            // Center of the arc: slightly above the defender's head
            double centerX = livingDefender.getX();
            double centerY = livingDefender.getY() + livingDefender.getBbHeight() * 0.9;
            double centerZ = livingDefender.getZ();
            Vec3 center = new Vec3(centerX, centerY, centerZ);

            // Radius scaled with entity size (tweak multiplier to taste)
            double baseRadius = Math.max(livingDefender.getBbWidth(), livingDefender.getBbHeight()) * 0.8;

            // particle count and speed
            int stepsPerSide = (int) Math.min(10, 10 * comboPercent);              // number of pairs on each side (total particles = 1 + 2*stepsPerSide)
            double maxAngle = Math.toRadians(Math.min(90, 90 * comboPercent)); // how far to each side (90deg -> left & right extremes)
            double speed = 0.14;                // particle speed outward from center

            // Spawn the center/top particle first (angle = 0)
            {
                double a = 0.0;
                // Position = center + upInPlane * cos(a)*radius + right * sin(a)*radius
                Vec3 pos = center.add(upInPlane.scale(Math.cos(a) * baseRadius))
                        .add(right.scale(Math.sin(a) * baseRadius));
                Vec3 dir = pos.subtract(center);
                if (dir.lengthSqr() > 1e-6) dir = dir.normalize().scale(speed);

                level.sendParticles(ParticleTypes.FLAME,
                        pos.x, pos.y, pos.z,
                        1,
                        dir.x, dir.y, dir.z,
                        0.0);
            }

            // Build outward from the center: spawn symmetric left/right particles for k=1..stepsPerSide
            for (int k = 1; k <= stepsPerSide; k++) {
                double t = (double) k / (double) stepsPerSide;    // 0..1
                double angle = t * maxAngle;                     // 0..maxAngle
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                // RIGHT side (positive sin)
                Vec3 posR = center.add(upInPlane.scale(cos * baseRadius))
                        .add(right.scale(sin * baseRadius));
                Vec3 dirR = posR.subtract(center);
                if (dirR.lengthSqr() > 1e-6) dirR = dirR.normalize().scale(speed);

                level.sendParticles(ParticleTypes.FLAME,
                        posR.x, posR.y, posR.z,
                        1,
                        dirR.x, dirR.y, dirR.z,
                        0.0);

                // LEFT side (negative sin)
                Vec3 posL = center.add(upInPlane.scale(cos * baseRadius))
                        .add(right.scale(-sin * baseRadius));
                Vec3 dirL = posL.subtract(center);
                if (dirL.lengthSqr() > 1e-6) dirL = dirL.normalize().scale(speed);

                level.sendParticles(ParticleTypes.FLAME,
                        posL.x, posL.y, posL.z,
                        1,
                        dirL.x, dirL.y, dirL.z,
                        0.0);
            }
        }
    }
}
