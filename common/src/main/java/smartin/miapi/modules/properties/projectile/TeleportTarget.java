package smartin.miapi.modules.properties.projectile;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property defines the teleportation behavior of projectiles that hit a target.
 * When a projectile with this property strikes a living entity, the target will be teleported
 * as if they had consumed a chorus fruit, which randomly teleports them to a nearby location.
 *
 * @header Teleport Target Projectile Property
 * @path /data_types/properties/projectile/teleport_target
 * @description_start
 * The Teleport Target Property adds the ability for projectiles to teleport any living entity they hit.
 * Upon impact, the entity is teleported using the same mechanic as consuming a chorus fruit.
 * If the property is true, and the hit entity is a living entity, the teleportation occurs.
 * The projectile is discarded after impact.
 * @description_end
 * @data teleport_target: A boolean value indicating whether the teleport behavior is enabled for the projectile.
 */

public class TeleportTarget extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("teleport_target");
    public static TeleportTarget property;

    public TeleportTarget() {
        super(KEY, false);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            ItemStack itemStack = new ItemStack(Items.CHORUS_FRUIT);
            if (isTrue(event.projectile.getPickupItem()) && event.entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                Items.CHORUS_FRUIT.finishUsingItem(itemStack, event.projectile.level(), livingEntity);
                if (!event.projectile.level().isClientSide()) {
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }
}
