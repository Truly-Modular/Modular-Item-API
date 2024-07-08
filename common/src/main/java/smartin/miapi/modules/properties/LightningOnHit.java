package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class LightningOnHit extends DoubleProperty {
    public static String KEY = "lightning";
    public LightningOnHit property;

    public LightningOnHit() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (!listener.livingEntity.level().isClientSide() && listener.damageSource.getEntity() instanceof LivingEntity attacker) {
                double lightningStrength = getForItems(attacker.getAllSlots());
                for (int i = 0; i < lightningStrength; i++) {
                    LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(listener.livingEntity.level());
                    assert lightningEntity != null;
                    lightningEntity.moveTo(Vec3.atBottomCenterOf(listener.livingEntity.blockPosition()));
                    lightningEntity.setCause(attacker instanceof ServerPlayer ? (ServerPlayer) attacker : null);
                    listener.livingEntity.level().addFreshEntity(lightningEntity);
                }
            }
            return EventResult.pass();
        });
    }
}
