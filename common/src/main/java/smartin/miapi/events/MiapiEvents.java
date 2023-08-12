package smartin.miapi.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;

public class MiapiEvents {
    public static Event<LivingHurt> LIVING_HURT = EventFactory.createEventResult();
    public static Event<LivingHurt> LIVING_HURT_AFTER = EventFactory.createEventResult();
    public static Event<EntityRide> START_RIDING = EventFactory.createLoop(); // only fires on successful rides, and is not cancellable (if I wanted to make it cancellable, i would add mixinextras)
    public static Event<EntityRide> STOP_RIDING = EventFactory.createLoop();
    public static Event<BlockCraftingStatUpdate> BLOCK_STAT_UPDATE = EventFactory.createEventResult();
    public static Event<ItemCraftingStatUpdate> ITEM_STAT_UPDATE = EventFactory.createEventResult();
    public static Event<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_HIT = EventFactory.createEventResult();
    public static Event<ModularProjectileEntityHit> MODULAR_PROJECTILE_ENTITY_POST_HIT = EventFactory.createEventResult();
    public static Event<ModularProjectileBlockHit> MODULAR_PROJECTILE_BLOCK_HIT = EventFactory.createEventResult();
    public static Event<ModularProjectileTick> MODULAR_PROJECTILE_TICK = EventFactory.createEventResult();
    public static Event<ModularBowShot> MODULAR_BOW_SHOT = EventFactory.createEventResult();
    public static Event<ModularBowShot> MODULAR_BOW_POST_SHOT = EventFactory.createEventResult();

    public static class LivingHurtEvent {
        public final LivingEntity livingEntity;
        public DamageSource damageSource;
        public float amount;

        public LivingHurtEvent(LivingEntity livingEntity, DamageSource damageSource, float amount) {
            this.livingEntity = livingEntity;
            this.damageSource = damageSource;
            this.amount = amount;

        }

        public ItemStack getCausingItemStack() {
            if (damageSource.getSource() instanceof ProjectileEntity projectile && (projectile instanceof ItemProjectile itemProjectile)) {
                return itemProjectile.asItemStack();

            }
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                return attacker.getMainHandStack();
            }
            return ItemStack.EMPTY;
        }
    }

    public static class ModularProjectileEntityHitEvent {
        public EntityHitResult entityHitResult;
        public ItemProjectile projectile;
        public DamageSource damageSource;
        public float damage;

        public ModularProjectileEntityHitEvent(EntityHitResult entityHitResult, ItemProjectile projectile, DamageSource damageSource, float damage) {
            this.entityHitResult = entityHitResult;
            this.projectile = projectile;
            this.damageSource = damageSource;
            this.damage = damage;
        }
    }

    public static class ModularProjectileBlockHitEvent {
        public BlockHitResult blockHitResult;
        public ItemProjectile projectile;

        public ModularProjectileBlockHitEvent(BlockHitResult blockHitResult, ItemProjectile projectile) {
            this.blockHitResult = blockHitResult;
            this.projectile = projectile;
        }
    }

    public static class ModularBowShotEvent {
        public ItemProjectile projectile;
        public ItemStack bowStack;
        public LivingEntity shooter;

        public ModularBowShotEvent(ItemProjectile projectile, ItemStack bowStack, LivingEntity shooter) {
            this.projectile = projectile;
            this.bowStack = bowStack;
            this.shooter = shooter;
        }
    }

    public interface ModularProjectileBlockHit {
        EventResult hit(ModularProjectileBlockHitEvent event);
    }

    public interface ModularProjectileTick {
        EventResult tick(ItemProjectile event);
    }

    public interface ModularBowShot {
        EventResult call(ModularBowShotEvent event);
    }

    public interface ModularProjectileEntityHit {
        EventResult hit(ModularProjectileEntityHitEvent event);
    }

    public interface LivingHurt {
        EventResult hurt(LivingHurtEvent event);
    }

    public interface BlockCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, @Nullable PlayerEntity player);
    }

    public interface ItemCraftingStatUpdate {
        EventResult call(ModularWorkBenchEntity bench, Iterable<ItemStack> inventory, @Nullable PlayerEntity player);
    }

    public interface EntityRide {
        void ride(Entity passenger, Entity vehicle);
    }
}
