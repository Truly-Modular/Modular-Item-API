package smartin.miapi.modules.abilities.util.ItemProjectile;

import dev.architectury.event.EventResult;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.abilities.util.ItemProjectile.ArrowHitBehaviour.EntityBounceBehaviour;
import smartin.miapi.modules.abilities.util.ItemProjectile.ArrowHitBehaviour.EntityPierceBehaviour;
import smartin.miapi.modules.abilities.util.ItemProjectile.ArrowHitBehaviour.ProjectileHitBehaviour;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
import smartin.miapi.registries.RegistryInventory;

public class ItemProjectile extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> SPEED_DAMAGE = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<ItemStack> THROWING_STACK = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<ItemStack> BOW_ITEM_STACK = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.ITEM_STACK);
    private static final TrackedData<Float> WATER_DRAG = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> PREFERRED_SLOT = DataTracker.registerData(ItemProjectile.class, TrackedDataHandlerRegistry.INTEGER);
    public ItemStack thrownStack = ItemStack.EMPTY;
    private boolean dealtDamage;
    public int returnTimer;
    public float waterDrag = 0.99f;
    public WrappedSoundEvent hitEntitySound = new WrappedSoundEvent(this.getHitSound(), 1.0f, 1.0f);
    public WrappedSoundEvent hitGroundSound = new WrappedSoundEvent(this.getHitSound(), 1.0f, 1.0f);
    public ProjectileHitBehaviour projectileHitBehaviour = new EntityBounceBehaviour();

    public ItemProjectile(EntityType<? extends Entity> entityType, World world) {
        super((EntityType<? extends PersistentProjectileEntity>) entityType, world);
    }

    public ItemProjectile(World world, LivingEntity owner, ItemStack stack) {
        super(RegistryInventory.itemProjectileType.get(), owner, world);
        this.thrownStack = stack.copy();
        this.dataTracker.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
        this.dataTracker.set(THROWING_STACK, thrownStack);
        this.dataTracker.set(BOW_ITEM_STACK, ItemStack.EMPTY);
        this.dataTracker.set(WATER_DRAG, waterDrag);
        this.dataTracker.set(SPEED_DAMAGE, true);
        this.dataTracker.set(PREFERRED_SLOT, -1);
    }

    public void setPreferredSlot(int slotID) {
        this.dataTracker.set(PREFERRED_SLOT, slotID);
    }

    public void setBowItem(ItemStack bowItem) {
        this.dataTracker.set(BOW_ITEM_STACK, bowItem.copy());
    }

    public ItemStack getBowItem() {
        return this.dataTracker.get(BOW_ITEM_STACK);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(THROWING_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(LOYALTY, (byte) 0);
        this.dataTracker.startTracking(ENCHANTED, false);
        this.dataTracker.startTracking(BOW_ITEM_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(WATER_DRAG, 0.99f);
        this.dataTracker.startTracking(SPEED_DAMAGE, true);
        this.dataTracker.startTracking(PREFERRED_SLOT, 0);
    }

    public boolean getSpeedDamage() {
        return this.dataTracker.get(SPEED_DAMAGE);
    }

    public void setSpeedDamage(boolean speedDamage) {
        this.dataTracker.set(SPEED_DAMAGE, speedDamage);
    }

    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        int loyaltyLevel = this.dataTracker.get(LOYALTY);
        if (loyaltyLevel > 0 && (this.dealtDamage || this.isNoClip()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d targetDir = entity.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + targetDir.y * 0.015 * (double) loyaltyLevel, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }

                double speedAdjustment = 0.05 * (double) loyaltyLevel;
                this.setVelocity(this.getVelocity().multiply(0.95).add(targetDir.normalize().multiply(speedAdjustment)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.returnTimer;
            }
        }

        super.tick();
    }

    private boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayerEntity) || !entity.isSpectator();
        } else {
            return false;
        }
    }

    public ItemStack asItemStack() {
        return this.dataTracker.get(THROWING_STACK).copy();
    }

    public boolean isEnchanted() {
        return this.dataTracker.get(ENCHANTED);
    }

    @Nullable
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
    }

    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float damage = getProjectileDamage();
        if (this.getPierceLevel() > 0) {
            projectileHitBehaviour = new EntityPierceBehaviour();
            this.setPierceLevel((byte) (this.getPierceLevel() - 1));
        } else {
            projectileHitBehaviour = new EntityBounceBehaviour();
        }

        Entity owner = this.getOwner();
        MiapiEvents.ModularArrowHitEvent event = new MiapiEvents.ModularArrowHitEvent(entityHitResult, this, this.getDamageSources().arrow(this, owner), damage);
        EventResult result = MiapiEvents.MODULAR_PROJECTILE_HIT.invoker().hit(event);
        if (result.interruptsFurtherEvaluation()) {
            return;
        }
        damage = event.damage;
        this.dealtDamage = true;
        if (entity.damage(event.damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity victim) {
                if (owner instanceof LivingEntity livingOwner) {
                    EnchantmentHelper.onUserDamaged(victim, livingOwner);
                    EnchantmentHelper.onTargetDamaged(livingOwner, victim);
                }

                this.onHit(victim);
            }
        }

        if (this.projectileHitBehaviour != null) {
            projectileHitBehaviour.onHit(this, entityHitResult.getEntity(), entityHitResult);
        }
        MiapiEvents.ModularArrowHitEvent postEvent = new MiapiEvents.ModularArrowHitEvent(event.entityHitResult, this, event.damageSource, damage);
        EventResult postResult = MiapiEvents.MODULAR_PROJECTILE_POST_HIT.invoker().hit(postEvent);
        if (postResult.interruptsFurtherEvaluation()) {
            return;
        }
        this.playSound(this.hitEntitySound.event(), this.hitEntitySound.volume(), this.hitEntitySound.pitch());
    }

    public float getProjectileDamage() {
        float damage = (float) getDamage();
        if (this.getSpeedDamage()) {
            float speed = (float) this.getVelocity().length();
            damage = damage * speed;
        }
        return damage;
    }

    public boolean hasChanneling() {
        return EnchantmentHelper.hasChanneling(this.thrownStack);
    }

    protected boolean tryPickup(PlayerEntity player) {
        int slotId = this.dataTracker.get(PREFERRED_SLOT);
        boolean earlyPickup = switch (this.pickupType) {
            case DISALLOWED:
                yield false;
            case ALLOWED: {
                player.getInventory().getStack(slotId);
                yield tryInsertAtSlot(player.getInventory(),this.asItemStack(),slotId);
            }
            case CREATIVE_ONLY: {
                yield player.getAbilities().creativeMode;
            }
        };
        return earlyPickup || super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && (tryInsertAtSlot(player.getInventory(),this.asItemStack(),slotId) || player.getInventory().insertStack(this.asItemStack()));
    }

    public boolean tryInsertAtSlot(PlayerInventory inventory,ItemStack stack,int slot){
        ItemStack inventoryStack = inventory.getStack(slot);
        if(inventoryStack.isEmpty()){
            return inventory.insertStack(slot, stack);
        }
        if(ItemStack.canCombine(inventoryStack,stack)){
            return inventory.insertStack(slot, stack);
        }
        return false;
    }

    public void setDamageToDeal(boolean hasDamage) {
        this.dealtDamage = !hasDamage;
    }

    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    public void onPlayerCollision(PlayerEntity player) {
        if (this.isOwner(player) || this.getOwner() == null) {
            super.onPlayerCollision(player);
        }

    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ThrownItem", 10)) {
            this.thrownStack = ItemStack.fromNbt(nbt.getCompound("ThrownItem"));
            this.dataTracker.set(THROWING_STACK, thrownStack);
        }
        if (nbt.contains("BowItem", 10)) {
            ItemStack bowItem = ItemStack.fromNbt(nbt.getCompound("BowItem"));
            this.dataTracker.set(BOW_ITEM_STACK, bowItem);
        }

        this.dealtDamage = nbt.getBoolean("DealtDamage");
        this.dataTracker.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.thrownStack));
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("ThrownItem", this.thrownStack.writeNbt(new NbtCompound()));
        nbt.put("BowItem", this.getBowItem().writeNbt(new NbtCompound()));
        nbt.putBoolean("DealtDamage", this.dealtDamage);
    }

    public void age() {
        int i = this.dataTracker.get(LOYALTY);
        if (this.pickupType != PickupPermission.ALLOWED || i <= 0) {
            super.age();
        }

    }

    protected float getDragInWater() {
        return waterDrag;
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }
}
