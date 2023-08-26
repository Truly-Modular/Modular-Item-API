package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import smartin.miapi.entity.arrowhitbehaviours.EntityStickBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property is responsible for teleporting a hit target of projectiles
 */
public class ArrowProperty implements ModuleProperty {
    public static final String KEY = "is_arrow";
    public static ArrowProperty property;
    public static EntityStickBehaviour entityStickBehaviour = new EntityStickBehaviour();

    public ArrowProperty() {
        property = this;
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
