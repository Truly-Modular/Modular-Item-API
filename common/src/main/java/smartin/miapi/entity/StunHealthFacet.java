package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.registries.RegistryInventory;


public class StunHealthFacet implements EntityFacet<NbtCompound> {
    private LivingEntity livingEntity;
    private float currentAmount = 20;
    public static final Identifier facetIdentifier = new Identifier(Miapi.MOD_ID, "stun_current_health");
    public static FacetKey<StunHealthFacet> KEY = FacetRegistry.register(facetIdentifier, StunHealthFacet.class);

    public StunHealthFacet(LivingEntity entity) {
        this.livingEntity = entity;
    }

    /**
     * take StunDamage
     *
     * @param stunDamage
     * @return
     */
    public void takeStunDamage(float stunDamage, LivingEntity attacker) {
        currentAmount -= stunDamage;
        if (currentAmount <= 0) {
            if (!livingEntity.hasStatusEffect(RegistryInventory.stunResistanceEffect)) {
                this.livingEntity.addStatusEffect(new StatusEffectInstance(RegistryInventory.stunEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunLength, 0, false, true), attacker);
            }
            currentAmount = getMaxAmount();
        }
    }

    public float getCurrentStunHealth() {
        return currentAmount;
    }

    public void tick() {
        if (livingEntity.age % 5 == 4) {
            currentAmount = Math.min(getCurrentStunHealth() + 2.0f, getMaxAmount());
            if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                this.sendToClient(serverPlayerEntity);
            }
        }
    }

    public int ticksSinceLastAttack() {
        int lastAttackedTime = ((LivingEntityAccessor) livingEntity).getLastAttackedTime();
        if (lastAttackedTime > livingEntity.age) {
            return livingEntity.age;
        }
        return livingEntity.age - lastAttackedTime;
    }

    public float getMaxAmount() {
        return (float) livingEntity.getAttributeValue(AttributeRegistry.STUN_MAX_HEALTH);
    }


    @Override
    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putFloat("miapi:stun_current_health", getCurrentStunHealth());
        return compound;
    }

    @Override
    public void loadNbt(NbtCompound nbt) {
        currentAmount = nbt.getFloat("miapi:stun_current_health");
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        // The FacetSyncPacket requires 3 things: the entity holding the facet, the facet key, and the facet instance.
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
