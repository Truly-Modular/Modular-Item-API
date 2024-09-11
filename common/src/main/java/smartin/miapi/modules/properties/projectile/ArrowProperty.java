package smartin.miapi.modules.properties.projectile;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.arrowhitbehaviours.EntityStickBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This Property designates a projectile as an arrow, modifying its behavior accordingly.
 *
 * @header Arrow Property
 * @path /data_types/properties/projectile/arrow
 * @description_start The Arrow Property modifies the behavior of projectiles to simulate arrow-like characteristics. This includes
 * adjusting projectile hit behavior and properties when interacting with entities. For example, arrows may have
 * specific behaviors such as sticking to targets or interacting differently with enchantments.
 * @description_end
 * @data is_arrow: Indicates whether the projectile behaves like an arrow, affecting its hit behavior and other attributes.
 */

public class ArrowProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("is_arrow");
    public static ArrowProperty property;
    public static EntityStickBehaviour entityStickBehaviour = new EntityStickBehaviour();

    public ArrowProperty() {
        super(KEY, false);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.register((projectile, nbtCompound) -> {
            if (isTrue(projectile.getPickupItem())) {
                nbtCompound.set(ItemProjectileEntity.SPEED_DAMAGE, false);
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            if (
                    isTrue(event.projectile.getPickupItem()) &&
                    event.entityHitResult.getEntity() instanceof LivingEntity livingEntity &&
                    livingEntity.level() instanceof ServerLevel serverWorld
            ) {
                if (event.projectile.getOwner() != null && EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverWorld, event.projectile.getPickupItem(), event.projectile.getOwner()) > 0) {

                } else {
                    event.projectile.projectileHitBehaviour = entityStickBehaviour;
                }
            }
            return EventResult.pass();
        });
    }
}
