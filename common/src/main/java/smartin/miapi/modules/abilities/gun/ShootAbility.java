package smartin.miapi.modules.abilities.gun;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ShootAbility implements ItemUseAbility<ShootAbility.GunAbilityContext> {

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        @Nullable GunAbilityContext context = getSpecialContext(itemStack);
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        if (getAmmo(itemStack).isEmpty()) {
            return 72000;
        }
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        @Nullable GunAbilityContext context = getSpecialContext(itemStack);

        // If the context is null, this item does not have the shoot ability
        if (context == null) {
            return InteractionResultHolder.pass(itemStack);
        }

        // Perform logic only on the server side
        if (!world.isClientSide) {
            // Check if the gun is empty
            if (getAmmo(itemStack).isEmpty()) {
                // Start the reload process
                startReload(world, user, itemStack, context);
            } else {
                // If full-auto mode is enabled, start continuous usage
                if (context.fullAuto) {
                    user.startUsingItem(hand);
                } else {
                    // Perform a single shot for semi-auto or non-full-auto mode
                    performShooting(world, user, itemStack, context);
                }
            }
        }

        return InteractionResultHolder.success(itemStack);
    }


    /**
     * Handles the reload logic (round or full reload).
     */
    private void startReload(Level world, Player user, ItemStack itemStack, GunAbilityContext context) {
        if (context.reloadTime == null) {
            return; // Reload logic cannot proceed without a defined reload time
        }

        // Trigger reload animation
        if (context.reloadAnim != null) {
            triggerAnimation(user, context.reloadAnim);
        }

        // Mark the player as starting the reload process
        user.startUsingItem(user.getUsedItemHand());
    }


    @Override
    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return; // Only handle logic for players
        }

        @Nullable GunAbilityContext context = getSpecialContext(stack);
        if (context == null) {
            return; // Item does not have the shoot ability
        }

        // Check if the player is out of ammo
        List<ItemStack> ammo = getAmmo(stack);
        if (ammo.isEmpty()) {
            // Handle reload logic
            handleReloadTick(world, player, stack, context, remainingUseTicks);
        } else if (context.fullAuto) {
            // Handle full-auto shooting logic
            handleFullAutoTick(world, player, stack, context, remainingUseTicks);
        }
    }

    private void handleFullAutoTick(Level world, Player user, ItemStack stack, GunAbilityContext context, int remainingUseTicks) {
        if (context.shootCooldown == null) {
            return; // Shooting logic cannot proceed without a defined cooldown
        }

        double cooldownTime = context.shootCooldown.getValue(); // Cooldown time in ticks
        int ticksElapsed = 72000 - remainingUseTicks; // Calculate ticks elapsed since start of usage

        // Only shoot when the cooldown period has elapsed
        if (ticksElapsed % cooldownTime == 0) {
            performShooting(world, user, stack, context);
        }
    }


    private void handleReloadTick(Level world, Player user, ItemStack stack, GunAbilityContext context, int remainingUseTicks) {
        if (context.reloadTime == null) {
            return; // Reload logic cannot proceed without reloadTime
        }

        double reloadTime = context.reloadTime.getValue(); // Default reload time to 20 ticks if not specified

        // Calculate ticks remaining for the reload process
        int ticksElapsed = 72000 - remainingUseTicks; // Max use time is assumed to be 72000

        if (ticksElapsed >= reloadTime) {
            // Reload is complete
            if (context.roundReload) {
                // Add a single round of ammo (round reload)
                addAmmo(stack, 1);
            } else {
                // Fully refill ammo (full reload)
                refillAmmo(stack);
            }

            // Reset animation and play reload sound
            resetAnimation(user);
            if (context.onReload != null) {
                world.playSound(null, user.getX(), user.getY(), user.getZ(), context.onReload, user.getSoundSource(), 1.0f, 1.0f);
            }

            // Stop using the item if reload is complete
            user.stopUsingItem();
        } else {
            // Trigger the reload animation if it's not already triggered
            if (ticksElapsed == 1 && context.reloadAnim != null) {
                triggerAnimation(user, context.reloadAnim);
            }
        }
    }


    /**
     * Handles the shooting logic for the gun.
     */
    private void performShooting(Level world, Player user, ItemStack itemStack, GunAbilityContext context) {
        if (context.projectileSpeed == null || context.paletteCount == null) {
            return; // Shooting cannot proceed without these properties
        }

        double projectileSpeed = context.projectileSpeed.getValue(); // Default to 1.0 if not specified
        int paletteCount = (int) context.paletteCount.getValue(); // Default to 1 if not specified

        // Simulate shooting multiple projectiles (like a shotgun) based on paletteCount
        for (int i = 0; i < paletteCount; i++) {
            shootProjectile(world, user, projectileSpeed, paletteCount, 0.1f, 0.05f); // Spread/inaccuracy as example
        }

        // Play shooting sound if defined
        if (context.onShoot != null) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), context.onShoot, user.getSoundSource(), 1.0f, 1.0f);
        }

        // Trigger shooting animation if defined
        if (context.shotAnim != null) {
            triggerAnimation(user, context.shotAnim);
        }

        // Apply cooldown if defined
        if (context.shootCooldown != null) {
            double cooldownTime = context.shootCooldown.getValue(); // Default to 0 if not specified
            if (cooldownTime > 0) {
                user.getCooldowns().addCooldown(itemStack.getItem(), (int) cooldownTime);
            }
        }
    }

    /**
     * Resets the player's animation after an action.
     */
    private void resetAnimation(Player user) {
        // Logic to reset animation (e.g., sending a packet or updating client state)
        System.out.printf("Animation reset for player: %s%n", user.getName().getString());
    }


    private void shootProjectile(Level world, Player shooter, double speed, int projectileCount, float spread, float inaccuracy) {
        // Loop to shoot multiple projectiles
        List<ItemStack> stack = new ArrayList<>(getAmmo(shooter.getWeaponItem()));
        if (stack.isEmpty()) {
            return;
        }
        ItemStack projectileStack = stack.removeFirst();
        if (projectileStack.getItem() instanceof ProjectileItem projectileItem) {
            for (int i = 0; i < projectileCount; i++) {
                Projectile projectile = projectileItem.asProjectile(world, shooter.getEyePosition(), projectileStack, Direction.EAST);
                // Calculate spread offsets for X and Y axes
                double spreadX = (world.getRandom().nextGaussian() - 0.5) * spread;
                double spreadY = (world.getRandom().nextGaussian() - 0.5) * spread;
                double spreadZ = (world.getRandom().nextGaussian() - 0.5) * spread;

                // Get the player's look vector
                Vec3 lookVector = shooter.getLookAngle();

                // Apply spread to the look vector
                Vec3 projectileMotion = new Vec3(
                        lookVector.x + spreadX,
                        lookVector.y + spreadY,
                        lookVector.z + spreadZ
                ).normalize().scale(speed);

                // Create and configure the projectile
                projectile.setOwner(shooter);
                projectile.setPos(shooter.getEyePosition().add(lookVector.scale(0.5))); // Spawn slightly in front of the shooter
                projectile.setDeltaMovement(projectileMotion);

                // Adjust projectile inaccuracy
                float randomInaccuracy = (float) (world.getRandom().nextGaussian() * inaccuracy);
                projectile.setDeltaMovement(
                        projectile.getDeltaMovement().x + randomInaccuracy,
                        projectile.getDeltaMovement().y + randomInaccuracy,
                        projectile.getDeltaMovement().z + randomInaccuracy
                );

                // Add the projectile to the world
                world.addFreshEntity(projectile);
            }
            setAmmo(shooter.getWeaponItem(), stack);
        }
    }


    private void triggerAnimation(Player user, ResourceLocation anim) {
        // Placeholder: Trigger animation logic here
        // Example: Send packet to client to play animation
        System.out.printf("Triggered animation: %s for player: %s\n", anim, user.getName().getString());
    }

    @Override
    public <K> GunAbilityContext decode(DynamicOps<K> ops, K prefix) {
        return null;
    }

    @Override
    public GunAbilityContext getDefaultContext() {
        return null;
    }


    public GunAbilityContext merge(GunAbilityContext left, GunAbilityContext right, MergeType mergeType) {
        return left.merge(left, right, mergeType);
    }

    /**
     * Placeholder method to add ammo (for round reload).
     */
    private void addAmmo(ItemStack itemStack, int count) {
        // Logic to add ammo to the itemStack
    }

    /**
     * Placeholder method to refill ammo (for full reload).
     */
    private void refillAmmo(ItemStack itemStack) {
        // Logic to fully reload the itemStack
    }

    public void setAmmo(ItemStack weapon, List<ItemStack> ammo) {

    }

    public List<ItemStack> getAmmo(ItemStack weapon) {
        return List.of();
    }

    public static class GunAbilityContext implements MergeAble<GunAbilityContext> {
        public SoundEvent onShoot;
        public SoundEvent onReload;
        public DoubleOperationResolvable projectileSpeed;
        public DoubleOperationResolvable paletteCount;
        public ResourceLocation shotAnim;
        public ResourceLocation reloadAnim;
        public DoubleOperationResolvable shootCooldown;
        public DoubleOperationResolvable reloadTime;
        public Predicate<ItemStack> ammoPredicate;
        public boolean roundReload = false;
        public boolean fullAuto = false;

        public GunAbilityContext merge(GunAbilityContext left, GunAbilityContext right, MergeType mergeType) {
            return null;
        }
    }
}
