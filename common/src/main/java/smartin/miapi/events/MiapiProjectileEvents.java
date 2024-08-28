package smartin.miapi.events;

import com.redpxnda.nucleus.event.PrioritizedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.entity.ItemProjectileEntity;

public final class MiapiProjectileEvents {
    public static final PrioritizedEvent<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_HIT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_POST_HIT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ModularProjectileBlockHit> MODULAR_PROJECTILE_BLOCK_HIT = PrioritizedEvent.createEventResult();
    /**
     * the following events are meant so you can easily extend the projectile entity with custom data trackers and save data
     */
    public static final PrioritizedEvent<ModularProjectileTick> MODULAR_PROJECTILE_TICK = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemProjectileCompound> MODULAR_PROJECTILE_NBT_WRITE = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemProjectileCompound> MODULAR_PROJECTILE_NBT_READ = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemProjectileDataTrackerBuilder> MODULAR_PROJECTILE_DATA_TRACKER_INIT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ItemProjectileDataTracker> MODULAR_PROJECTILE_DATA_TRACKER_SET = PrioritizedEvent.createEventResult();

    public static final PrioritizedEvent<PlayerPickupEvent> MODULAR_PROJECTILE_PICK_UP = PrioritizedEvent.createEventResult();

    public static final PrioritizedEvent<CrossbowContext> MODULAR_CROSSBOW_PRE_SHOT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<CrossbowContextEvent> MODULAR_CROSSBOW_LOAD = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<CrossbowContextEvent> MODULAR_CROSSBOW_LOAD_AFTER = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<CrossbowContext> MODULAR_CROSSBOW_POST_SHOT = PrioritizedEvent.createEventResult();

    public static final PrioritizedEvent<ModularBowShot> MODULAR_BOW_SHOT = PrioritizedEvent.createEventResult();
    public static final PrioritizedEvent<ModularBowShot> MODULAR_BOW_POST_SHOT = PrioritizedEvent.createEventResult();

    public static class ModularProjectileEntityHitEvent {
        public EntityHitResult entityHitResult;
        public ItemProjectileEntity projectile;
        @Nullable
        public DamageSource damageSource;
        public float damage;

        public ModularProjectileEntityHitEvent(EntityHitResult entityHitResult, ItemProjectileEntity projectile, @Nullable DamageSource damageSource, float damage) {
            this.entityHitResult = entityHitResult;
            this.projectile = projectile;
            this.damageSource = damageSource;
            this.damage = damage;
        }
    }

    public static class ModularProjectileBlockHitEvent {
        public BlockHitResult blockHitResult;
        public ItemProjectileEntity projectile;

        public ModularProjectileBlockHitEvent(BlockHitResult blockHitResult, ItemProjectileEntity projectile) {
            this.blockHitResult = blockHitResult;
            this.projectile = projectile;
        }
    }

    public static class ModularBowShotEvent {
        public AbstractArrow projectile;
        public ItemStack bowStack;
        public LivingEntity shooter;

        public ModularBowShotEvent(AbstractArrow projectile, ItemStack bowStack, LivingEntity shooter) {
            this.projectile = projectile;
            this.bowStack = bowStack;
            this.shooter = shooter;
        }
    }

    public interface CrossbowContext {
        EventResult shoot(LivingEntity player, ItemStack crossbow);
    }

    public interface CrossbowContextEvent {
        EventResult load(CrossbowLoadingContext context);
    }

    public static class CrossbowLoadingContext {
        public LivingEntity player;
        public ItemStack crossbow;
        public ItemStack loadingProjectile;
        public EquipmentSlot crossbowSlot;

        public CrossbowLoadingContext(LivingEntity player, ItemStack crossbow, ItemStack loadingProjectile, EquipmentSlot crossbowSlot) {
            this.player = player;
            this.crossbow = crossbow;
            this.loadingProjectile = loadingProjectile;
            this.crossbowSlot = crossbowSlot;
        }
    }

    public interface ItemProjectileCompound {
        EventResult nbtEvent(ItemProjectileEntity projectile, CompoundTag nbtCompound, RegistryAccess registryAccess);
    }

    public interface PlayerPickupEvent {
        EventResult pickup(Player entity, ItemProjectileEntity projectile);
    }

    public interface ItemProjectileDataTracker {
        EventResult dataTracker(ItemProjectileEntity projectile, SynchedEntityData nbtCompound);
    }

    public interface ItemProjectileDataTrackerBuilder {
        EventResult dataTracker(SynchedEntityData.Builder nbtCompound);
    }

    public interface ModularProjectileBlockHit {
        EventResult hit(ModularProjectileBlockHitEvent event);
    }

    public interface ModularProjectileTick {
        EventResult tick(ItemProjectileEntity event);
    }

    public interface ModularBowShot {
        EventResult call(ModularBowShotEvent event);
    }

    public interface ModularProjectileEntityHit {
        EventResult hit(ModularProjectileEntityHitEvent event);
    }
}
