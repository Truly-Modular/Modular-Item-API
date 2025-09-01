package smartin.miapi.entity;

import com.redpxnda.nucleus.facet.FacetKey;
import com.redpxnda.nucleus.facet.FacetRegistry;
import com.redpxnda.nucleus.facet.entity.EntityFacet;
import com.redpxnda.nucleus.facet.network.clientbound.FacetSyncPacket;
import com.redpxnda.nucleus.network.PlayerSendable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;

public class EntitySpeedFacetFix implements EntityFacet<CompoundTag> {
    private final Entity livingEntity;

    private long worldTime;     // Stored world time
    private long realTime;      // Stored system time (ms)
    private Vec3 velocity;      // Stored velocity

    public static final ResourceLocation facetIdentifier = Miapi.id("entity_speed_facet_fix");
    public static final FacetKey<EntitySpeedFacetFix> KEY = FacetRegistry.register(facetIdentifier, EntitySpeedFacetFix.class);

    public EntitySpeedFacetFix(Entity entity) {
        this.livingEntity = entity;
        this.velocity = Vec3.ZERO;
    }

    /**
     * Update values and sync to client.
     */
    public void updateAndSync() {
        this.worldTime = livingEntity.level().getGameTime();
        this.realTime = System.currentTimeMillis();
        this.velocity = livingEntity.getDeltaMovement();
        this.sendToTrackers(livingEntity);
    }

    /**
     * Check if this facet's stored data is still valid compared to the current entity state.
     * - world time must not differ by more than 3 ticks
     * - real time must not differ by more than 10 seconds
     *
     * @return true if still valid, false otherwise
     */
    public boolean validate() {
        long currentWorld = livingEntity.level().getGameTime();
        long currentReal = System.currentTimeMillis();

        long worldDiff = Math.abs(currentWorld - this.worldTime);
        long realDiff = Math.abs(currentReal - this.realTime);

        return worldDiff <= 3 && realDiff <= 10_000;
    }

    public long getWorldTime() {
        return worldTime;
    }

    public long getRealTime() {
        return realTime;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("miapi:world_time", worldTime);
        tag.putLong("miapi:real_time", realTime);

        CompoundTag velTag = new CompoundTag();
        velTag.putDouble("x", velocity.x);
        velTag.putDouble("y", velocity.y);
        velTag.putDouble("z", velocity.z);

        tag.put("miapi:velocity", velTag);
        return tag;
    }

    @Override
    public void loadNbt(CompoundTag nbt) {
        this.worldTime = nbt.getLong("miapi:world_time");
        this.realTime = nbt.getLong("miapi:real_time");

        if (nbt.contains("miapi:velocity")) {
            CompoundTag velTag = nbt.getCompound("miapi:velocity");
            this.velocity = new Vec3(
                    velTag.getDouble("x"),
                    velTag.getDouble("y"),
                    velTag.getDouble("z")
            );
        } else {
            this.velocity = Vec3.ZERO;
        }
    }

    @Override
    public PlayerSendable createPacket(Entity target) {
        return new FacetSyncPacket<>(target, KEY, this);
    }
}
