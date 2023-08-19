package smartin.miapi.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import smartin.miapi.item.modular.items.projectile.ItemProjectileEntity;

public final class MiapiProjectileEvents {
    public static final Event<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_HIT = EventFactory.createEventResult();
    public static final Event<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_POST_HIT = EventFactory.createEventResult();
    public static final Event<ModularProjectileBlockHit> MODULAR_PROJECTILE_BLOCK_HIT = EventFactory.createEventResult();
    public static final Event<ModularProjectileTick> MODULAR_PROJECTILE_TICK = EventFactory.createEventResult();
    public static final Event<ItemProjectileCompound> MODULAR_PROJECTILE_NBT_WRITE = EventFactory.createEventResult();
    public static final Event<ItemProjectileCompound> MODULAR_PROJECTILE_NBT_READ = EventFactory.createEventResult();
    public static final Event<ItemProjectileDataTracker> MODULAR_PROJECTILE_DATA_TRACKER_INIT = EventFactory.createEventResult();
    public static final Event<ItemProjectileDataTracker> MODULAR_PROJECTILE_DATA_TRACKER_SET = EventFactory.createEventResult();
    public static final Event<PlayerPickupEvent> MODULAR_PROJECTILE_PICK_UP = EventFactory.createEventResult();

    public static final Event<ModularBowShot> MODULAR_BOW_SHOT = EventFactory.createEventResult();
    public static final Event<ModularBowShot> MODULAR_BOW_POST_SHOT = EventFactory.createEventResult();

    public static class ModularProjectileEntityHitEvent {
        public EntityHitResult entityHitResult;
        public ItemProjectileEntity projectile;
        public DamageSource damageSource;
        public float damage;

        public ModularProjectileEntityHitEvent(EntityHitResult entityHitResult, ItemProjectileEntity projectile, DamageSource damageSource, float damage) {
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
        public PersistentProjectileEntity projectile;
        public ItemStack bowStack;
        public LivingEntity shooter;

        public ModularBowShotEvent(PersistentProjectileEntity projectile, ItemStack bowStack, LivingEntity shooter) {
            this.projectile = projectile;
            this.bowStack = bowStack;
            this.shooter = shooter;
        }
    }

    public interface ItemProjectileCompound {
        EventResult nbtEvent(ItemProjectileEntity projectile, NbtCompound nbtCompound);
    }

    public interface PlayerPickupEvent {
        EventResult pickup(PlayerEntity entity, ItemProjectileEntity projectile);
    }

    public interface ItemProjectileDataTracker {
        EventResult dataTracker(ItemProjectileEntity projectile, DataTracker nbtCompound);
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
