package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.arrowhitbehaviours.EntityStickBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property is responsible for designating a Projectile as an arrow, having subtle changes to its behaviour
 */
public class ArrowProperty implements ModuleProperty {
    public static final String KEY = "is_arrow";
    public static ArrowProperty property;
    public static EntityStickBehaviour entityStickBehaviour = new EntityStickBehaviour();

    public ArrowProperty() {
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.register((projectile, nbtCompound) -> {
            JsonElement element = ItemModule.getMergedProperty(projectile.getPickupItem(), property);
            if (element != null && !element.getAsBoolean()) {
                nbtCompound.set(ItemProjectileEntity.SPEED_DAMAGE, false);
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.register((projectile, nbtCompound) -> {
            JsonElement element = ItemModule.getMergedProperty(projectile.getPickupItem(), property);
            if (element != null && !element.getAsBoolean()) {
                nbtCompound.set(ItemProjectileEntity.SPEED_DAMAGE, false);
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            JsonElement element = ItemModule.getMergedProperty(event.projectile.getPickupItem(), property);
            if (
                    element != null &&
                    element.getAsBoolean() &&
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

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsBoolean();
        return true;
    }
}
