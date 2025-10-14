package smartin.miapi.modules.properties.onHit;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;

/**
 * A facet that tracks combo hits between entities.
 * It stores the target entity being comboed, the combo count,
 * and the tick when the last hit happened.
 */
public class ComboFacet implements EntityFacet<CompoundTag> {
    private final LivingEntity livingEntity;

    /**
     * The entity that this player is currently comboing (if any).
     */
    private Entity comboTarget;

    /**
     * The number of consecutive hits in the combo.
     */
    private int comboCount = 0;

    /**
     * The game tick when the last successful hit occurred.
     */
    private int lastHitTick = 0;

    private int ticksUntilReset = 20;

    public static final ResourceLocation FACET_ID = Miapi.id("combo_tracker");
    public static final FacetKey<ComboFacet> KEY = FacetRegistry.register(FACET_ID, ComboFacet.class);

    public ComboFacet(LivingEntity entity) {
        this.livingEntity = entity;
    }

    /**
     * Called when this entity hits another entity.
     */
    public void registerHit(LivingEntity target, int maxCombo) {
        if (comboTarget != null && comboTarget.getId() == target.getId()) {
            comboCount = Math.min(maxCombo, comboCount + 1);
        } else {
            comboTarget = target;
            comboCount = 1;
        }

        lastHitTick = livingEntity.tickCount;

        // Sync to client if server-side
        if (livingEntity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            sendToClient(livingEntity, serverPlayer);
        }
    }

    /**
     * Called every tick.
     * Automatically resets combo if too much time passes since the last hit.
     */
    public void tick() {
        // Reset combo if more than 40 ticks (2 seconds) have passed since last hit
        if (comboCount > 0 && livingEntity.tickCount - lastHitTick > ticksUntilReset) {
            resetCombo();
        }
    }

    /**
     * Called every tick.
     * Automatically resets combo if too much time passes since the last hit.
     */
    public void setTicksUntilReset(int ticksUntilReset) {
        this.ticksUntilReset = ticksUntilReset;
    }

    /**
     * Resets the combo count and clears the target.
     */
    public void resetCombo() {
        comboCount = 0;
        comboTarget = null;
        if (livingEntity instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            sendToClient(livingEntity, serverPlayer);
            serverPlayer.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 0.3f);
        }
    }

    public int getComboCount() {
        return comboCount;
    }

    public int getLastHitTick() {
        return lastHitTick;
    }

    public Entity getComboTarget() {
        return comboTarget;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("combo_count", comboCount);
        tag.putInt("last_hit_tick", lastHitTick);
        if (comboTarget != null) {
            tag.putInt("combo_target_id", comboTarget.getId());
        }
        return tag;
    }

    @Override
    public void loadNbt(CompoundTag tag) {
        comboCount = tag.getInt("combo_count");
        lastHitTick = tag.getInt("last_hit_tick");
        if (tag.contains("combo_target_id")) {
            int targetId = tag.getInt("combo_target_id");
            Entity possibleTarget = livingEntity.level().getEntity(targetId);
            if (possibleTarget != null) {
                comboTarget = possibleTarget;
            }
        }
    }

    @Override
    public void sendToClient(Entity capHolder, ServerPlayer player) {
        if (player != null && player.connection != null) {
            try {
                createPacket(capHolder).send(player);
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("ComboFacet sync issue", e);
            }
        }
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
