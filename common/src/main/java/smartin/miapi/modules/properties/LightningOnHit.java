package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class LightningOnHit extends DoubleProperty {
    public static String KEY = "lightning";
    public LightningOnHit property;

    public LightningOnHit() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (!listener.livingEntity.getWorld().isClient() && listener.damageSource.getAttacker() instanceof LivingEntity attacker) {
                double lightningStrength = getForItems(attacker.getItemsEquipped());
                for(int i = 0;i<lightningStrength;i++){
                    LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(listener.livingEntity.getWorld());
                    assert lightningEntity != null;
                    lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(listener.livingEntity.getBlockPos()));
                    lightningEntity.setChanneler(attacker instanceof ServerPlayerEntity ? (ServerPlayerEntity) attacker : null);
                    listener.livingEntity.getWorld().spawnEntity(lightningEntity);
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
