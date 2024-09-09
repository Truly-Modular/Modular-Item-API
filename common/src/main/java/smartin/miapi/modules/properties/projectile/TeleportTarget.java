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
 * This property is responsible for teleporting a hit target of projectiles
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