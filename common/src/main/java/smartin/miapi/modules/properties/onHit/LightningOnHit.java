package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property triggers lightning strikes at the location of the entity being hit.
 *
 * @header Lightning On Hit Property
 * @path /data_types/properties/on_hit/lightning
 * @description_start
 * The Lightning On Hit Property causes lightning bolts to strike the location of the entity that was hit when the attacker has this property active.
 * This effect is applied every time the entity with the property deals damage, making it a dramatic and powerful feature for weapons or abilities.
 * The number of lightning bolts and their intensity are determined by the value of the property.
 * @description_end
 * @data value: The number of lightning bolts that will strike the location of the entity being hit. For example, a value of 3 means three lightning bolts will appear.
 */

public class LightningOnHit extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("lightning");
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
