package smartin.miapi.entity;

import dev.architectury.event.EventResult;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.arrowhitbehaviours.EntityBounceBehaviour;
import smartin.miapi.entity.arrowhitbehaviours.EntityPierceBehaviour;
import smartin.miapi.entity.arrowhitbehaviours.ProjectileHitBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
import smartin.miapi.modules.properties.AirDragProperty;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.registries.RegistryInventory;

public class ItemProjectileEntity extends PersistentProjectileEntity {
    public static final TrackedData<Byte> LOYALTY = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> SPEED_DAMAGE = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<ItemStack> THROWING_STACK = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    public static final TrackedData<ItemStack> BOW_ITEM_STACK = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
    public static final TrackedData<Float> WATER_DRAG = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.FLOAT);
    public static final TrackedData<Integer> PREFERRED_SLOT = DataTracker.registerData(ItemProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public ItemStack thrownStack = ItemStack.EMPTY;
    private boolean dealtDamage;
    public int returnTimer;
    public float waterDrag = 0.99f;
    public WrappedSoundEvent hitEntitySound = new WrappedSoundEvent(this.getHitSound(), 1.0f, 1.0f);
    public ProjectileHitBehaviour projectileHitBehaviour = new EntityBounceBehaviour();

    public ItemProjectileEntity(EntityType<? extends Entity> entityType, World world) {
        super((EntityType<? extends PersistentProjectileEntity>) entityType, world);
    }

    public ItemProjectileEntity(World world, Position position, ItemStack itemStack) {
        super(RegistryInventory.itemProjectileType.get(), position.getX(), position.getY(), position.getZ(), world);
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        this.thrownStack = stack;
        this.dataTracker.set(THROWING_STACK, thrownStack);
        this.dataTracker.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
        this.checkDespawn();
        setup();
    }

    public ItemProjectileEntity(World world, LivingEntity owner, ItemStack itemStack) {
        super(RegistryInventory.itemProjectileType.get(), owner, world);
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        this.thrownStack = stack.copy();
        this.dataTracker.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
        this.dataTracker.set(ENCHANTED, stack.hasGlint());
        this.dataTracker.set(THROWING_STACK, thrownStack);
        this.dataTracker.set(BOW_ITEM_STACK, ItemStack.EMPTY);
        this.dataTracker.set(WATER_DRAG, waterDrag);
        this.dataTracker.set(SPEED_DAMAGE, true);
        this.dataTracker.set(PREFERRED_SLOT, -1);
        if (getBowItem().isEmpty() && owner != null) {
            setBowItem(owner.getActiveItem());
        }
        setup();
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.invoker().dataTracker(this, this.getDataTracker());
    }

    private void setup() {
        ItemStack projectileStack = this.asItemStack();
        this.setDamage(AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE));
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

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(LOYALTY, (byte) 0);
        this.dataTracker.startTracking(ENCHANTED, false);
        this.dataTracker.startTracking(THROWING_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(BOW_ITEM_STACK, ItemStack.EMPTY);
        this.dataTracker.startTracking(WATER_DRAG, 0.99f);
        this.dataTracker.startTracking(SPEED_DAMAGE, true);
        this.dataTracker.startTracking(PREFERRED_SLOT, 0);
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.invoker().dataTracker(this, this.getDataTracker());
    }

    public boolean getSpeedDamage() {
        return this.dataTracker.get(SPEED_DAMAGE);
    }

    public void setSpeedDamage(boolean speedDamage) {
        this.dataTracker.set(SPEED_DAMAGE, speedDamage);
    }

    @Override
    public void tick() {
        ItemStack asItem = asItemStack();
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_TICK.invoker().tick(this).interruptsFurtherEvaluation()) {
            return;
        }
        if (this.inGroundTime > 4) {
            this.setVelocity(new Vec3d(0, 0, 0));
            this.dealtDamage = true;
        }
        if (this.getBlockPos().getY() < this.getWorld().getBottomY() - 50 && MiapiConfig.EnchantmentGroup.betterLoyalty.getValue()) {
            //loyalty in void
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
                this.setPos(this.getX(), this.getY() + targetDir.y * 0.015 * loyaltyLevel, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }

                double speedAdjustment = 0.05 * loyaltyLevel;
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

    protected void age() {
        if (this.age >= 1200 * 20) {
            this.discard();
        }
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

    @Override
    @Nullable
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
    }

    @Override
    public void setVelocity(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        ItemStack projectileStack = this.asItemStack();
        speed = (float) Math.max(0.1, speed + AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED));
        divergence *= (float) Math.pow(12.0, -AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY));
        float f = -MathHelper.sin(yaw * ((float) Math.PI / 180)) * MathHelper.cos(pitch * ((float) Math.PI / 180));
        float g = -MathHelper.sin((pitch + roll) * ((float) Math.PI / 180));
        float h = MathHelper.cos(yaw * ((float) Math.PI / 180)) * MathHelper.cos(pitch * ((float) Math.PI / 180));
        this.setVelocity(f, g, h, speed, divergence);
        Vec3d vec3d = shooter.getVelocity();
        this.setVelocity(this.getVelocity().add(vec3d.x, shooter.isOnGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    @Override
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
        MiapiProjectileEvents.ModularProjectileEntityHitEvent event =
                new MiapiProjectileEvents.ModularProjectileEntityHitEvent(
                        entityHitResult,
                        this,
                        this.getDamageSources().arrow(this, owner),
                        damage);
        EventResult result = MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.invoker().hit(event);
        if (result.interruptsFurtherEvaluation()) {
            return;
        }
        damage = event.damage;
        this.dealtDamage = true;
        if (entity.damage(event.damageSource, (float) Math.ceil(damage))) {
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
        MiapiProjectileEvents.ModularProjectileEntityHitEvent postEvent =
                new MiapiProjectileEvents.ModularProjectileEntityHitEvent(
                        event.entityHitResult,
                        this,
                        event.damageSource,
                        damage);
        EventResult postResult = MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_POST_HIT.invoker().hit(postEvent);
        if (postResult.interruptsFurtherEvaluation()) {
            return;
        }
        this.playSound(this.hitEntitySound.event(), this.hitEntitySound.volume(), this.hitEntitySound.pitch());
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.invoker().hit(
                new MiapiProjectileEvents.ModularProjectileBlockHitEvent(blockHitResult, this)).interruptsFurtherEvaluation()) {
            return;
        }
        super.onBlockHit(blockHitResult);
    }

    public float getProjectileDamage() {
        float damage = (float) getDamage();
        if (this.getSpeedDamage()) {
            float speed = (float) this.getVelocity().length();
            damage = (float) MathHelper.clamp((double) speed * damage, 0.0, 2.147483647E9);
        }
        return damage;
    }

    public boolean hasChanneling() {
        return EnchantmentHelper.hasChanneling(this.thrownStack);
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        int slotId = this.dataTracker.get(PREFERRED_SLOT);
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_PICK_UP.invoker().pickup(player, this).interruptsFurtherEvaluation()) {
            return false;
        }
        switch (this.pickupType) {
            case DISALLOWED -> {
                return false;
            }
            case CREATIVE_ONLY -> {
                return player.getAbilities().creativeMode;
            }
            case ALLOWED -> {
                boolean hasLoyalty = this.dataTracker.get(LOYALTY) > 0;
                if (hasLoyalty && getOwner() != null && !isOwner(player)) {
                    return false;
                }
                if (slotId >= 0 && player.getInventory().getStack(slotId).isEmpty()) {
                    return player.getInventory().insertStack(slotId, this.asItemStack());
                } else {
                    return player.getInventory().insertStack(this.asItemStack());
                }
            }
            default -> {
                return super.tryPickup(player) || this.isNoClip() && this.isOwner(player) && player.getInventory().insertStack(this.asItemStack());
            }
        }
    }

    public void setDamageToDeal(boolean hasDamage) {
        this.dealtDamage = !hasDamage;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
        super.onPlayerCollision(player);
        /*
        if (this.dataTracker.getRaw(LOYALTY).intValue() > 0) {
            if (this.isOwner(player) || this.getOwner() == null) {
                super.onPlayerCollision(player);
            }
        }
         */
    }

    @Override
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
        if (nbt.contains("WaterDrag")) {
            this.dataTracker.set(WATER_DRAG, nbt.getFloat("WaterDrag"));
        }
        if (nbt.contains("SpeedDamage")) {
            this.dataTracker.set(SPEED_DAMAGE, nbt.getBoolean("SpeedDamage"));
        }
        if (nbt.contains("PreferredSlot")) {
            this.dataTracker.set(PREFERRED_SLOT, nbt.getInt("PreferredSlot"));
        }

        this.dealtDamage = nbt.getBoolean("DealtDamage");
        this.dataTracker.set(LOYALTY, (byte) EnchantmentHelper.getLoyalty(this.thrownStack));
        MiapiProjectileEvents.MODULAR_PROJECTILE_NBT_READ.invoker().nbtEvent(this, nbt);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("ThrownItem", this.thrownStack.writeNbt(new NbtCompound()));
        nbt.put("BowItem", this.getBowItem().writeNbt(new NbtCompound()));
        nbt.putBoolean("DealtDamage", this.dealtDamage);
        nbt.putFloat("WaterDrag", this.dataTracker.get(WATER_DRAG));
        nbt.putBoolean("SpeedDamage", this.dataTracker.get(SPEED_DAMAGE));
        nbt.putInt("PreferredSlot", this.dataTracker.get(PREFERRED_SLOT));
        MiapiProjectileEvents.MODULAR_PROJECTILE_NBT_WRITE.invoker().nbtEvent(this, nbt);
    }

    @Override
    protected float getDragInWater() {
        return waterDrag;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }
}
