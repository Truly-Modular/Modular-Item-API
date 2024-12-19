package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import dev.architectury.impl.NetworkAggregator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.mixin.LivingEntityAccessor;
import smartin.miapi.registries.RegistryInventory;


public class StunHealthFacet implements EntityFacet<CompoundTag> {
    private final LivingEntity livingEntity;
    private float currentAmount = 20;
    public static final ResourceLocation facetIdentifier = Miapi.id("stun_current_health");
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
            if (!livingEntity.hasEffect(RegistryInventory.stunResistanceEffect)) {
                this.livingEntity.addEffect(new MobEffectInstance(RegistryInventory.stunEffect, MiapiConfig.INSTANCE.server.stunEffectCategory.stunLength, 0, false, true), attacker);
            }
            currentAmount = getMaxAmount();
        }
    }

    public float getCurrentStunHealth() {
        return currentAmount;
    }

    public void tick() {
        if (livingEntity.tickCount % 5 == 4) {
            currentAmount = Math.min(getCurrentStunHealth() + 2.0f, getMaxAmount());
            if (livingEntity instanceof ServerPlayer serverPlayerEntity && serverPlayerEntity.connection != null) {
                this.sendToClient(serverPlayerEntity);
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
        return (float) livingEntity.getAttributeValue(AttributeRegistry.STUN_MAX_HEALTH);
    }


    @Override
    public CompoundTag toNbt() {
        CompoundTag compound = new CompoundTag();
        compound.putFloat("miapi:stun_current_health", getCurrentStunHealth());
        return compound;
    }

    @Override
    public void sendToClient(Entity capHolder, ServerPlayer player) {
        if (player != null && player.connection != null && player.level() != null) {
            try {
                createPacket(capHolder).send(player);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("facet sync issue", e);
            }
        }
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        currentAmount = nbt.getFloat("miapi:stun_current_health");
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        // The FacetSyncPacket requires 3 things: the entity holding the facet, the facet key, and the facet instance.
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
