package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property is responsible for teleporting a hit target of projectiles
 */
public class TeleportTarget implements ModuleProperty {
    public static final String KEY = "teleport_target";
    public static TeleportTarget property;

    public TeleportTarget() {
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            ItemStack itemStack = new ItemStack(Items.CHORUS_FRUIT);
            JsonElement element = ItemModule.getMergedProperty(event.projectile.getPickupItem(),property);
            if (element != null && element.getAsBoolean() &&  event.entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                Items.CHORUS_FRUIT.finishUsingItem(itemStack, event.projectile.level(), livingEntity);
                if(!event.projectile.level().isClientSide()){
                    event.projectile.discard();
                }
                return EventResult.interruptTrue();
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
