package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.arrowhitbehaviours.EntityStickBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
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
            JsonElement element = ItemModule.getMergedProperty(projectile.asItemStack(), property);
            if (element != null && !element.getAsBoolean()) {
                nbtCompound.set(ItemProjectileEntity.SPEED_DAMAGE, false);
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.register((projectile, nbtCompound) -> {
            JsonElement element = ItemModule.getMergedProperty(projectile.asItemStack(), property);
            if (element != null && !element.getAsBoolean()) {
                nbtCompound.set(ItemProjectileEntity.SPEED_DAMAGE, false);
            }

            projectile.hitEntitySound = new WrappedSoundEvent(SoundEvents.ENTITY_ARROW_HIT, 1, 1);
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            JsonElement element = ItemModule.getMergedProperty(event.projectile.asItemStack(), property);
            if (element != null && element.getAsBoolean() && event.entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                if (EnchantmentHelper.getLoyalty(event.projectile.asItemStack()) < 1) {
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
