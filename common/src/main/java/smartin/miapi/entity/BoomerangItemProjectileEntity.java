package smartin.miapi.entity;

import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.AirDragProperty;

import java.util.LinkedHashSet;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BoomerangItemProjectileEntity extends ItemProjectileEntity {
    public LinkedHashSet<Entity> targets = new LinkedHashSet<>();
    public Entity currentTarget = null;

    public BoomerangItemProjectileEntity(EntityType<? extends Entity> entityType, Level world) {
        super((EntityType<? extends AbstractArrow>) entityType, world);
    }

    public BoomerangItemProjectileEntity(Level world, Position position, ItemStack itemStack) {
        super(world, position, itemStack);
    }

    public BoomerangItemProjectileEntity(Level world, LivingEntity owner, ItemStack itemStack) {
        super(world, owner, itemStack);
    }

    public void setTargets(LinkedHashSet<Entity> targets) {
        this.targets = targets;
    }

    @Override
    public void tick() {
        ItemStack asItem = getPickupItem();
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_TICK.invoker().tick(this).interruptsFurtherEvaluation()) {
            return;
        }

        Entity entity = getTarget();
        if ((this.dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 targetDir = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + targetDir.y * 0.015, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double speedAdjustment = 0.05;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(targetDir.normalize().scale(speedAdjustment)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.returnTimer;
            }
        }

        Vec3 vec3d = this.getDeltaMovement();
        float m = (float) AirDragProperty.property.getValueSafe(asItem);
        if (this.isInWater()) {
            m = 1.0f;
        }
        this.setDeltaMovement(vec3d.scale(m));

        super.tick();
    }

    @Nullable
    public Entity getTarget() {
        if (currentTarget != null) {
            currentTarget = targets.stream().findFirst().get();
            if(currentTarget!=null){
                currentTarget = getOwner();
            }
        }
        return currentTarget;
    }
}
