package smartin.miapi.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.AirDragProperty;

import java.util.LinkedHashSet;

public class BoomerangItemProjectileEntity extends ItemProjectileEntity {
    public LinkedHashSet<Entity> targets = new LinkedHashSet<>();
    public Entity currentTarget = null;

    public BoomerangItemProjectileEntity(EntityType<? extends Entity> entityType, World world) {
        super((EntityType<? extends PersistentProjectileEntity>) entityType, world);
    }

    public BoomerangItemProjectileEntity(World world, Position position, ItemStack itemStack) {
        super(world, position, itemStack);
    }

    public BoomerangItemProjectileEntity(World world, LivingEntity owner, ItemStack itemStack) {
        super(world, owner, itemStack);
    }

    public void setTargets(LinkedHashSet<Entity> targets) {
        this.targets = targets;
    }

    @Override
    public void tick() {
        ItemStack asItem = asItemStack();
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_TICK.invoker().tick(this).interruptsFurtherEvaluation()) {
            return;
        }

        Entity entity = getTarget();
        if ((this.dealtDamage || this.isNoClip()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d targetDir = entity.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + targetDir.y * 0.015, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }

                double speedAdjustment = 0.05;
                this.setVelocity(this.getVelocity().multiply(0.95).add(targetDir.normalize().multiply(speedAdjustment)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.returnTimer;
            }
        }

        Vec3d vec3d = this.getVelocity();
        float m = (float) AirDragProperty.property.getValueSafe(asItem);
        if (this.isTouchingWater()) {
            m = 1.0f;
        }
        this.setVelocity(vec3d.multiply(m));

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
