package smartin.miapi.attributes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import smartin.miapi.Miapi;

import java.util.Map;
import java.util.WeakHashMap;

public class ElytraAttributes {
    public static Map<LivingEntity, Vec3d> velocityMap = new WeakHashMap<>();


    public static void movementUpdate(LivingEntity livingEntity) {
        if (!livingEntity.isLogicalSideForUpdatingMovement()) {
            return;
        }
        if (velocityMap.containsKey(livingEntity)) {
            if (isElytraFlying(livingEntity)) {
                Vec3d lastVelocity = from(velocityMap.get(livingEntity));
                Vec3d currentVelocity = from(livingEntity.getVelocity());
                double glideEfficiency = livingEntity.getAttributes().getValue(AttributeRegistry.ELYTRA_GLIDE_EFFICIENCY) / 100;
                double turnEfficiency = livingEntity.getAttributes().getValue(AttributeRegistry.ELYTRA_TURN_EFFICIENCY) / 100;
                double lastSpeed = lastVelocity.length();
                double currentSpeed = currentVelocity.length();

                if (Math.abs(Math.max(0.1, lastSpeed) / Math.max(0.1, currentSpeed) - 1) > 0.9) {
                    //more than 90% change, prob a collision or sth
                    velocityMap.put(livingEntity, livingEntity.getVelocity());
                    return;
                }
                double horizontalDotProduct = currentVelocity.normalize().dotProduct(new Vec3d(0.0, 1.0, 0.0));
                double horizontalRatio = Math.min(1, 1 + horizontalDotProduct);
                if (horizontalDotProduct > 0.1) {
                    horizontalRatio = 0;
                }

                double directionChange = Math.min(1, ((1 - from(lastVelocity).normalize().dotProduct(from(currentVelocity).normalize())) * 100));

                glideEfficiency = glideEfficiency * horizontalRatio;

                double speedLoss = Math.max(0.0, lastSpeed - currentSpeed);
                double speedRecovery = Math.min(1.0, (directionChange * turnEfficiency + (1 - directionChange) * glideEfficiency));
                double speed = Math.max(0.00000001, currentSpeed + speedLoss * speedRecovery);

                Vec3d vec3d = from(currentVelocity);
                vec3d = from(vec3d.normalize());
                vec3d = vec3d.multiply(speed);
                livingEntity.setVelocity(vec3d);

                velocityMap.put(livingEntity, livingEntity.getVelocity());

            } else {
                velocityMap.remove(livingEntity);
            }
        } else if (isElytraFlying(livingEntity)) {
            velocityMap.put(livingEntity, livingEntity.getVelocity());
        }
    }

    private static Vec3d from(Vec3d vec3d) {

        double x = Double.isNaN(vec3d.x) ? 0 : vec3d.x;
        double y = Double.isNaN(vec3d.y) ? 0 : vec3d.y;
        double z = Double.isNaN(vec3d.z) ? 0 : vec3d.z;

        return new Vec3d(x, y, z);
    }

    static boolean isElytraFlying(LivingEntity livingEntity) {
        return livingEntity.isFallFlying();
    }
}
