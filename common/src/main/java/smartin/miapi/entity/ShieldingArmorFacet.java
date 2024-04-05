package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.mixin.LivingEntityAccessor;


public class ShieldingArmorFacet implements EntityFacet<NbtCompound> {
    private LivingEntity livingEntity;
    private float currentAmount;
    public static final Identifier facetIdentifier = new Identifier(Miapi.MOD_ID, "shielding_armor");
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
        if (livingEntity.age % 5 == 3) {
            if (
                    ticksSinceLastAttack() > 100
            ) {
                currentAmount = Math.min(getCurrentAmount() + 0.25f, getMaxAmount());
                if (livingEntity instanceof PlayerEntity) {
                    Miapi.LOGGER.info("tick " + currentAmount + " max " + getMaxAmount());
                }
                if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity) {
                    Miapi.LOGGER.info("sync to client");
                    this.sendToClient(serverPlayerEntity);
                }
            }
        }
    }

    public int ticksSinceLastAttack() {
        int lastAttackedTime = ((LivingEntityAccessor) livingEntity).getLastAttackedTime();
        if (lastAttackedTime > livingEntity.age) {
            //return livingEntity.age;
        }
        return livingEntity.age - lastAttackedTime;
    }

    public float getMaxAmount() {
        return (float) livingEntity.getAttributeValue(AttributeRegistry.SHIELDING_ARMOR);
    }


    @Override
    public NbtCompound toNbt() {
        NbtCompound compound = new NbtCompound();
        compound.putFloat("miapi:shielding_armor_current", getCurrentAmount());
        return compound;
    }

    @Override
    public void loadNbt(NbtCompound nbt) {
        currentAmount = nbt.getFloat("miapi:shielding_armor_current");
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        // The FacetSyncPacket requires 3 things: the entity holding the facet, the facet key, and the facet instance.
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
