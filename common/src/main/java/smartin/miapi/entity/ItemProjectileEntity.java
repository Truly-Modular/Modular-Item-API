package smartin.miapi.entity;

import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.arrowhitbehaviours.EntityBounceBehaviour;
import smartin.miapi.entity.arrowhitbehaviours.EntityPierceBehaviour;
import smartin.miapi.entity.arrowhitbehaviours.ProjectileHitBehaviour;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.AbstractArrowAccessor;
import smartin.miapi.modules.abilities.util.WrappedSoundEvent;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.projectile.AirDragProperty;
import smartin.miapi.modules.properties.projectile.ChannelingProperty;
import smartin.miapi.modules.properties.projectile.MakesImpactSoundProperty;
import smartin.miapi.registries.RegistryInventory;

public class ItemProjectileEntity extends AbstractArrow {
    public static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<Boolean> ENCHANTED = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SPEED_DAMAGE = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<ItemStack> THROWING_STACK = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<ItemStack> BOW_ITEM_STACK = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<Float> WATER_DRAG = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> PREFERRED_SLOT = SynchedEntityData.defineId(ItemProjectileEntity.class, EntityDataSerializers.INT);
    public ItemStack thrownStack = ItemStack.EMPTY;
    protected boolean dealtDamage;
    public int returnTimer;
    public float waterDrag = 0.99f;
    public WrappedSoundEvent hitEntitySound = new WrappedSoundEvent(this.getDefaultHitGroundSoundEvent(), 1.0f, 1.0f);
    public ProjectileHitBehaviour projectileHitBehaviour = new EntityBounceBehaviour();

    public ItemProjectileEntity(EntityType<? extends Entity> entityType, Level world) {
        super((EntityType<? extends AbstractArrow>) entityType, world);
    }

    public ItemProjectileEntity(Level world, Position position, ItemStack itemStack) {
        super(RegistryInventory.registeredItemProjectileType, world);
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        this.thrownStack = stack;
        this.entityData.set(THROWING_STACK, thrownStack);
        this.entityData.set(LOYALTY, this.getLoyaltyFromItem(stack));
        this.entityData.set(ENCHANTED, stack.hasFoil());
        this.checkDespawn();
        setup();
    }

    public ItemProjectileEntity(Level world, LivingEntity owner, ItemStack itemStack, ItemStack weapon) {
        super(
                RegistryInventory.registeredItemProjectileType, owner, world, itemStack, weapon);
        ItemStack stack = itemStack.copy();
        stack.setCount(1);
        this.thrownStack = stack.copy();
        this.entityData.set(LOYALTY, this.getLoyaltyFromItem(stack));
        this.entityData.set(ENCHANTED, stack.hasFoil());
        this.entityData.set(THROWING_STACK, thrownStack);
        this.entityData.set(BOW_ITEM_STACK, weapon);
        this.entityData.set(WATER_DRAG, waterDrag);
        this.entityData.set(SPEED_DAMAGE, true);
        this.entityData.set(PREFERRED_SLOT, -1);
        if (getBowItem().isEmpty() && owner != null) {
            setBowItem(owner.getUseItem());
        }
        setup();
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_SET.invoker().dataTracker(this, this.getEntityData());
    }

    private void setup() {
        ItemStack projectileStack = this.getPickupItem();
        this.setBaseDamage(AttributeUtil.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE.value()));
    }

    private byte getLoyaltyFromItem(ItemStack stack) {
        Level var3 = this.level();
        if (var3 instanceof ServerLevel serverLevel) {
            return (byte) Mth.clamp(EnchantmentHelper.getTridentReturnToOwnerAcceleration(serverLevel, stack, this), 0, 127);
        } else {
            return 0;
        }
    }

    public void setPreferredSlot(int slotID) {
        this.entityData.set(PREFERRED_SLOT, slotID);
    }

    public void setBowItem(ItemStack bowItem) {
        this.entityData.set(BOW_ITEM_STACK, bowItem.copy());
    }

    public ItemStack getBowItem() {
        return this.entityData.get(BOW_ITEM_STACK);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOYALTY, (byte) 0);
        builder.define(ENCHANTED, false);
        builder.define(THROWING_STACK, ItemStack.EMPTY);
        builder.define(BOW_ITEM_STACK, ItemStack.EMPTY);
        builder.define(WATER_DRAG, 0.99f);
        builder.define(SPEED_DAMAGE, true);
        builder.define(PREFERRED_SLOT, 0);
        MiapiProjectileEvents.MODULAR_PROJECTILE_DATA_TRACKER_INIT.invoker().dataTracker(builder);
    }

    public boolean getSpeedDamage() {
        return this.entityData.get(SPEED_DAMAGE);
    }

    public void setSpeedDamage(boolean speedDamage) {
        this.entityData.set(SPEED_DAMAGE, speedDamage);
    }

    @Override
    public void tick() {
        ItemStack asItem = getPickupItem();
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_TICK.invoker().tick(this).interruptsFurtherEvaluation()) {
            return;
        }
        if (this.inGroundTime > 4) {
            this.setDeltaMovement(new Vec3(0, 0, 0));
            this.dealtDamage = true;
        }
        if (this.blockPosition().getY() < this.level().getMinBuildHeight() - 50 && MiapiConfig.INSTANCE.server.enchants.betterLoyalty) {
            //loyalty in void
            this.dealtDamage = true;
        }

        Entity entity = this.getOwner();
        int loyaltyLevel = this.entityData.get(LOYALTY);
        if (loyaltyLevel > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.level().isClientSide && this.pickup == Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 targetDir = entity.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + targetDir.y * 0.015 * loyaltyLevel, this.getZ());
                if (this.level().isClientSide) {
                    this.yOld = this.getY();
                }

                double speedAdjustment = 0.05 * loyaltyLevel;
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

    protected void tickDespawn() {
        ++this.tickCount;
        if (this.tickCount >= 1200 * 20) {
            this.discard();
        }
    }

    protected boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        } else {
            return false;
        }
    }

    @Override
    public ItemStack getDefaultPickupItem() {
        return this.entityData.get(THROWING_STACK).copy();
    }

    @Override
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 currentPosition, Vec3 nextPosition) {
        return this.dealtDamage ? null : super.findHitEntity(currentPosition, nextPosition);
    }

    @Override
    public void shootFromRotation(Entity shooter, float pitch, float yaw, float roll, float speed, float divergence) {
        ItemStack projectileStack = this.getPickupItem();
        speed = (float) Math.max(0.1, speed + AttributeUtil.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()));
        divergence *= (float) Math.pow(12.0, -AttributeUtil.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY.value()));
        float f = -Mth.sin(yaw * ((float) Math.PI / 180)) * Mth.cos(pitch * ((float) Math.PI / 180));
        float g = -Mth.sin((pitch + roll) * ((float) Math.PI / 180));
        float h = Mth.cos(yaw * ((float) Math.PI / 180)) * Mth.cos(pitch * ((float) Math.PI / 180));
        this.shoot(f, g, h, speed, divergence);
        Vec3 vec3d = shooter.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3d.x, shooter.onGround() ? 0.0 : vec3d.y, vec3d.z));
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity defender = entityHitResult.getEntity();
        float damage = getProjectileDamage();
        if (this.getPierceLevel() > 0) {
            projectileHitBehaviour = new EntityPierceBehaviour();
            ((AbstractArrowAccessor) this).callSetPierceLevel((byte) (this.getPierceLevel() - 1));
        } else {
            projectileHitBehaviour = new EntityBounceBehaviour();
        }

        Entity owner = this.getOwner();
        MiapiProjectileEvents.ModularProjectileEntityHitEvent event =
                new MiapiProjectileEvents.ModularProjectileEntityHitEvent(
                        entityHitResult,
                        this,
                        this.damageSources().arrow(this, owner),
                        damage);
        EventResult result = MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.invoker().hit(event);
        if (result.interruptsFurtherEvaluation()) {
            if (this.projectileHitBehaviour != null) {
                projectileHitBehaviour.onHit(this, entityHitResult.getEntity(), entityHitResult);
            }
            return;
        }
        damage = event.damage;
        this.dealtDamage = true;
        if (defender.hurt(event.damageSource, (float) Math.ceil(damage))) {
            if (defender.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, defender, event.damageSource, this.getWeaponItem());
            }

            if (defender instanceof LivingEntity victim) {
                this.doKnockback(victim, event.damageSource);
                this.doPostHurtEffects(victim);
            }
        }
        if (ChannelingProperty.hasChanneling(this.getPickupItem())) {
            LightningBolt lightningEntity = EntityType.LIGHTNING_BOLT.create(this.level());
            assert lightningEntity != null;
            lightningEntity.moveTo(Vec3.atBottomCenterOf(entityHitResult.getEntity().getOnPos()));
            if (this.getOwner() instanceof ServerPlayer serverPlayer) {
                lightningEntity.setCause(serverPlayer);
            }
            this.level().addFreshEntity(lightningEntity);
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

    public ItemStack getPickupItem() {
        ItemStack stack = this.entityData.get(THROWING_STACK);
        if (stack != null && !stack.isEmpty()) {
            return stack;
        }
        return ItemStack.EMPTY;
        //return super.getPickupItem();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.invoker().hit(
                new MiapiProjectileEvents.ModularProjectileBlockHitEvent(blockHitResult, this)).interruptsFurtherEvaluation()) {
            if (this.projectileHitBehaviour != null) {
                projectileHitBehaviour.onBlockHit(this, blockHitResult);
            }
            return;
        }
        if (this.projectileHitBehaviour != null) {
            projectileHitBehaviour.onBlockHit(this, blockHitResult);
        }
        super.onHitBlock(blockHitResult);
    }

    @Override
    protected void onHit(HitResult result) {
        hitEntitySound = new WrappedSoundEvent(this.getDefaultHitGroundSoundEvent(), 1.0f, 1.0f);
        ((AbstractArrowAccessor) this).setSoundEvent(hitEntitySound.event());
        HitResult.Type hitresult$type = result.getType();
        boolean makeEvent = MakesImpactSoundProperty.property.isTrue(getPickupItem());
        if (hitresult$type == HitResult.Type.ENTITY) {
            EntityHitResult entityhitresult = (EntityHitResult) result;
            Entity entity = entityhitresult.getEntity();
            if (entity.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && entity instanceof Projectile) {
                Projectile projectile = (Projectile) entity;
                projectile.deflect(ProjectileDeflection.AIM_DEFLECT, this.getOwner(), this.getOwner(), true);
            }

            this.onHitEntity(entityhitresult);
            getPickupItem();
            if (makeEvent) {
                this.level().gameEvent(GameEvent.PROJECTILE_LAND, result.getLocation(), GameEvent.Context.of(this, (BlockState) null));
            }
        } else if (hitresult$type == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult) result;
            this.onHitBlock(blockhitresult);
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (makeEvent) {
                this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockpos, GameEvent.Context.of(this, this.level().getBlockState(blockpos)));
            }
        }

    }

    public float getProjectileDamage() {
        float damage = (float) getBaseDamage();
        if (this.getSpeedDamage()) {
            float speed = (float) this.getDeltaMovement().length();
            damage = (float) Mth.clamp((double) speed * damage, 0.0, 2.147483647E9);
        }
        return damage;
    }

    @Override
    protected boolean tryPickup(Player player) {
        int slotId = this.entityData.get(PREFERRED_SLOT);
        if (MiapiProjectileEvents.MODULAR_PROJECTILE_PICK_UP.invoker().pickup(player, this).interruptsFurtherEvaluation()) {
            return false;
        }
        switch (this.pickup) {
            case DISALLOWED -> {
                return false;
            }
            case CREATIVE_ONLY -> {
                if (getLoyaltyFromItem(this.getPickupItem()) > 0 && this.ownedBy(player)) {
                    return true;
                }
                return player.getAbilities().instabuild;
            }
            case ALLOWED -> {
                boolean hasLoyalty = this.entityData.get(LOYALTY) > 0;
                if (hasLoyalty && getOwner() != null && !ownedBy(player)) {
                    return false;
                }
                if (slotId == -2 && player.getOffhandItem().isEmpty()) {
                    player.getInventory().offhand.set(0, this.getPickupItem());
                    player.getInventory().setChanged();
                    return true;
                }
                if (slotId >= 0 && player.getInventory().getItem(slotId).isEmpty()) {
                    return player.getInventory().add(slotId, this.getPickupItem());
                } else {
                    return player.getInventory().add(this.getPickupItem());
                }
            }
            default -> {
                return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
            }
        }
    }

    public void setDamageToDeal(boolean hasDamage) {
        this.dealtDamage = !hasDamage;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        boolean makeEvent = MakesImpactSoundProperty.property.isTrue(getPickupItem());
        if (!makeEvent) {
            return SoundEvents.EMPTY;
        }
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        /*
        if (this.dataTracker.getRaw(LOYALTY).intValue() > 0) {
            if (this.isOwner(player) || this.getOwner() == null) {
                super.onPlayerCollision(player);
            }
        }
         */
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("ThrownItem", 10)) {
            this.thrownStack = ItemStack.parse(registryAccess(), nbt.getCompound("ThrownItem")).get();
            this.entityData.set(THROWING_STACK, thrownStack);
        } else {
            this.thrownStack = ItemStack.EMPTY;
            this.entityData.set(THROWING_STACK, ItemStack.EMPTY);
        }
        if (nbt.contains("BowItem", 10)) {
            ItemStack bowItem = ItemStack.parse(registryAccess(), nbt.getCompound("BowItem")).get();
            this.entityData.set(BOW_ITEM_STACK, bowItem);
        } else {
            this.entityData.set(BOW_ITEM_STACK, ItemStack.EMPTY);
        }
        if (nbt.contains("WaterDrag")) {
            this.entityData.set(WATER_DRAG, nbt.getFloat("WaterDrag"));
        }
        if (nbt.contains("SpeedDamage")) {
            this.entityData.set(SPEED_DAMAGE, nbt.getBoolean("SpeedDamage"));
        }
        if (nbt.contains("PreferredSlot")) {
            this.entityData.set(PREFERRED_SLOT, nbt.getInt("PreferredSlot"));
        }

        this.dealtDamage = nbt.getBoolean("DealtDamage");
        this.entityData.set(LOYALTY, getLoyaltyFromItem(this.thrownStack));
        MiapiProjectileEvents.MODULAR_PROJECTILE_NBT_READ.invoker().nbtEvent(this, nbt, registryAccess());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (!thrownStack.isEmpty()) {
            nbt.put("ThrownItem", this.thrownStack.save(this.registryAccess(), new CompoundTag()));
        }
        if (!this.getBowItem().isEmpty()) {

            nbt.put("BowItem", this.getBowItem().save(this.registryAccess(), new CompoundTag()));
        }
        nbt.putBoolean("DealtDamage", this.dealtDamage);
        nbt.putFloat("WaterDrag", this.entityData.get(WATER_DRAG));
        nbt.putBoolean("SpeedDamage", this.entityData.get(SPEED_DAMAGE));
        nbt.putInt("PreferredSlot", this.entityData.get(PREFERRED_SLOT));
        MiapiProjectileEvents.MODULAR_PROJECTILE_NBT_WRITE.invoker().nbtEvent(this, nbt, registryAccess());
    }

    @Override
    protected float getWaterInertia() {
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
