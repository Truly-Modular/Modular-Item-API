package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.mixin.LivingEntityAccessor;


public class ShieldingArmorFacet implements EntityFacet<CompoundTag> {
    private final LivingEntity livingEntity;
    private float currentAmount;
    public static final ResourceLocation facetIdentifier = Miapi.id("shielding_armor");
    public static FacetKey<ShieldingArmorFacet> KEY = FacetRegistry.register(facetIdentifier, ShieldingArmorFacet.class);

    public ShieldingArmorFacet(LivingEntity entity) {
        this.livingEntity = entity;
    }

    /**
     * return the damage that pierces Shielding Armor
     *
     * @param originalDamage
     * @return
     */
    public float takeDamage(float originalDamage) {
        float reduction = Math.min(originalDamage, getCurrentAmount());
        currentAmount -= reduction;
        return originalDamage - reduction;
    }

    public float getCurrentAmount() {
        return currentAmount;
    }

    public void tick() {
        if (livingEntity.tickCount % 5 == 3) {
            if (
                    ticksSinceLastAttack() > 100
            ) {
                currentAmount = Math.min(getCurrentAmount() + 0.25f, getMaxAmount());
                if (livingEntity instanceof ServerPlayer serverPlayerEntity && serverPlayerEntity.connection != null) {
                    this.sendToClient(serverPlayerEntity);
                }
            }
        }
    }

    public int ticksSinceLastAttack() {
        int lastAttackedTime = ((LivingEntityAccessor) livingEntity).getLastAttackedTime();
        if (lastAttackedTime > livingEntity.tickCount) {
            return livingEntity.tickCount;
        }
        return livingEntity.tickCount - lastAttackedTime;
    }

    public float getMaxAmount() {
        return (float) livingEntity.getAttributeValue(AttributeRegistry.SHIELDING_ARMOR);
    }


    @Override
    public CompoundTag toNbt() {
        CompoundTag compound = new CompoundTag();
        compound.putFloat("miapi:shielding_armor_current", getCurrentAmount());
        return compound;
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        currentAmount = nbt.getFloat("miapi:shielding_armor_current");
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        // The FacetSyncPacket requires 3 things: the entity holding the facet, the facet key, and the facet instance.
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
