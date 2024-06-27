package smartin.miapi.modules.abilities;

import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.CrossbowProperty;

import java.util.List;
import java.util.function.Predicate;

/**
 * An ability that allows the usage of the Item like a crossbow
 */
public class CrossbowAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    private boolean charged = false;
    private boolean loaded = false;
    private static final String PROJECTILE_KEY = "ChargedProjectiles";

    public CrossbowAbility() {
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        CrossbowProperty.CrossbowAbilityConfig config = CrossbowProperty.getConfig(itemStack);
        if (true) {
            Miapi.LOGGER.warn("testing if ALLOWED");
            return true;
        }
        return config != null && player.getInventory().hasAnyMatching(config.ammoPredicate);
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.CROSSBOW;
    }

    private static float getPullProgress(int useTicks, ItemStack stack) {
        float f = (float) useTicks / (float) getPullTime(stack);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getMaxUseTime(ItemStack stack) {
        return getPullTime(stack) + 3;
    }

    public static int getPullTime(ItemStack itemStack) {
        CrossbowProperty.CrossbowAbilityConfig config = CrossbowProperty.getConfig(itemStack);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, itemStack);
        return i == 0 ? 25 : 25 - 5 * i;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        Miapi.LOGGER.warn("USE");
        ItemStack itemStack = user.getItemInHand(hand);
        CrossbowProperty.CrossbowAbilityConfig config = CrossbowProperty.getConfig(itemStack);
        if (isCharged(itemStack)) {
            shootAll(world, user, hand, itemStack, getSpeed(itemStack), 1.0F);
            setCharged(itemStack, false);
            return InteractionResultHolder.consume(itemStack);
        } else if (!getProjectile(config.ammoPredicate, user).isEmpty()) {
            if (!isCharged(itemStack)) {
                this.charged = false;
                this.loaded = false;
                user.startUsingItem(hand);
            }

            return InteractionResultHolder.consume(itemStack);
        } else {
            Miapi.LOGGER.warn("USE FAIL");
            return InteractionResultHolder.fail(itemStack);
        }
    }

    public ItemStack getProjectile(Predicate<ItemStack> predicate, Player entity) {
        ItemStack itemStack = ProjectileWeaponItem.getHeldProjectile(entity, predicate);
        if (!itemStack.isEmpty()) {
            return itemStack;
        } else {

            for (int i = 0; i < entity.getInventory().getContainerSize(); ++i) {
                ItemStack itemStack2 = entity.getInventory().getItem(i);
                if (predicate.test(itemStack2)) {
                    return itemStack2;
                }
            }

            return entity.getAbilities().instabuild ? new ItemStack(Items.ARROW) : ItemStack.EMPTY;
        }
    }

    private static float getSpeed(ItemStack stack) {
        return hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        Miapi.LOGGER.warn("Stop Using");
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        float f = getPullProgress(i, stack);
        Miapi.LOGGER.warn(String.valueOf(f));
        if (f >= 1.0F && !isCharged(stack) && loadProjectiles(user, stack)) {
            setCharged(stack, true);
            SoundSource soundCategory = user instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            world.playSound((Player) null, user.getX(), user.getY(), user.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundCategory, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    private static boolean loadProjectiles(LivingEntity shooter, ItemStack projectile) {
        Miapi.LOGGER.warn("trying to load Projectile");
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, projectile);
        int j = i == 0 ? 1 : 3;
        boolean bl = shooter instanceof Player playerEntity && playerEntity.getAbilities().instabuild;
        ItemStack itemStack = shooter.getProjectile(projectile);
        ItemStack itemStack2 = itemStack.copy();

        for (int k = 0; k < j; ++k) {
            if (k > 0) {
                itemStack = itemStack2.copy();
            }

            if (itemStack.isEmpty() && bl) {
                itemStack = new ItemStack(Items.ARROW);
                itemStack2 = itemStack.copy();
            }

            if (!loadProjectile(shooter, projectile, itemStack, k > 0, bl)) {
                Miapi.LOGGER.warn("failed to load Projectile");
                return false;
            }
        }

        return true;
    }

    private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
        if (projectile.isEmpty()) {
            return false;
        } else {
            boolean bl = creative && projectile.getItem() instanceof ArrowItem;
            ItemStack itemStack;
            if (!bl && !creative && !simulated) {
                itemStack = projectile.split(1);
                if (projectile.isEmpty() && shooter instanceof Player player) {
                    player.getInventory().removeItem(projectile);
                }
            } else {
                itemStack = projectile.copy();
            }

            putProjectile(crossbow, itemStack);
            return true;
        }
    }

    public static boolean isCharged(ItemStack stack) {
        CompoundTag nbtCompound = stack.getNbt();
        return nbtCompound != null && nbtCompound.getBoolean("Charged");
    }

    public static void setCharged(ItemStack stack, boolean charged) {
        CompoundTag nbtCompound = stack.getOrCreateNbt();
        nbtCompound.putBoolean("Charged", charged);
    }

    private static void putProjectile(ItemStack crossbow, ItemStack projectile) {
        CompoundTag nbtCompound = crossbow.getOrCreateNbt();
        ListTag nbtList;
        if (nbtCompound.contains(PROJECTILE_KEY, 9)) {
            nbtList = nbtCompound.getList(PROJECTILE_KEY, 10);
        } else {
            nbtList = new ListTag();
        }

        CompoundTag nbtCompound2 = new CompoundTag();
        projectile.writeNbt(nbtCompound2);
        nbtList.add(nbtCompound2);
        nbtCompound.put(PROJECTILE_KEY, nbtList);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains(PROJECTILE_KEY, 9)) {
            ListTag nbtList = nbtCompound.getList(PROJECTILE_KEY, 10);
            if (nbtList != null) {
                for (int i = 0; i < nbtList.size(); ++i) {
                    CompoundTag nbtCompound2 = nbtList.getCompound(i);
                    list.add(ItemStack.parse(nbtCompound2));
                }
            }
        }

        return list;
    }

    private static void clearProjectiles(ItemStack crossbow) {
        CompoundTag nbtCompound = crossbow.getNbt();
        if (nbtCompound != null) {
            ListTag nbtList = nbtCompound.getList(PROJECTILE_KEY, 9);
            nbtList.clear();
            nbtCompound.put(PROJECTILE_KEY, nbtList);
        }

    }

    public static boolean hasProjectile(ItemStack crossbow, Item projectile) {
        return getProjectiles(crossbow).stream().anyMatch((s) -> {
            return s.is(projectile);
        });
    }

    private static void shoot(Level world, LivingEntity shooter, InteractionHand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        if (!world.isClientSide) {
            boolean bl = projectile.is(Items.FIREWORK_ROCKET);
            Projectile projectileEntity;
            if (bl) {
                projectileEntity = new FireworkRocketEntity(world, projectile, shooter, shooter.getX(), shooter.getEyeY() - 0.15000000596046448, shooter.getZ(), true);
            } else {
                projectileEntity = createArrow(world, shooter, crossbow, projectile);
                if (creative || simulated != 0.0F) {
                    ((AbstractArrow) projectileEntity).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (shooter instanceof CrossbowAttackMob crossbowUser) {
                crossbowUser.performCrossbowAttack(crossbowUser.getTarget(), crossbow, projectileEntity, simulated);
            } else {
                Vec3 vec3d = shooter.getUpVector(1.0F);
                Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((simulated * 0.017453292F), vec3d.x, vec3d.y, vec3d.z);
                Vec3 vec3d2 = shooter.getViewVector(1.0F);
                Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
                projectileEntity.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
            }

            crossbow.hurtAndBreak(bl ? 3 : 1, shooter, (e) -> {
                e.sendToolBreakStatus(hand);
            });
            world.addFreshEntity(projectileEntity);
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, soundPitch);
        }
    }

    private static AbstractArrow createArrow(Level world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
        ArrowItem arrowItem = (ArrowItem) (arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
        AbstractArrow persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
        if (entity instanceof Player) {
            persistentProjectileEntity.setCritArrow(true);
        }

        persistentProjectileEntity.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        persistentProjectileEntity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte) i);
        }

        return persistentProjectileEntity;
    }

    public static void shootAll(Level world, LivingEntity entity, InteractionHand hand, ItemStack stack, float speed, float divergence) {
        List<ItemStack> list = getProjectiles(stack);
        float[] fs = getSoundPitches(entity.getRandom());

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemStack = list.get(i);
            boolean bl = entity instanceof Player playerEntity && playerEntity.getAbilities().instabuild;
            if (!itemStack.isEmpty()) {
                if (i == 0) {
                    shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 0.0F);
                } else if (i == 1) {
                    shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, -10.0F);
                } else if (i == 2) {
                    shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 10.0F);
                }
            }
        }

        postShoot(world, entity, stack);
    }

    private static float[] getSoundPitches(RandomSource random) {
        boolean bl = random.nextBoolean();
        return new float[]{1.0F, getSoundPitch(bl, random), getSoundPitch(!bl, random)};
    }

    private static float getSoundPitch(boolean flag, RandomSource random) {
        float f = flag ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static void postShoot(Level world, LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayer serverPlayerEntity) {
            if (!world.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }

            serverPlayerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }

        clearProjectiles(stack);
    }

    public void usageTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClientSide) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
            SoundEvent soundEvent = this.getQuickChargeSound(i);
            SoundEvent soundEvent2 = i == 0 ? SoundEvents.CROSSBOW_LOADING_MIDDLE : null;
            float f = (float) (stack.getUseDuration() - remainingUseTicks) / (float) getPullTime(stack);
            if (f < 0.2F) {
                this.charged = false;
                this.loaded = false;
            }

            if (f >= 0.2F && !this.charged) {
                this.charged = true;
                world.playSound((Player) null, user.getX(), user.getY(), user.getZ(), soundEvent, SoundSource.PLAYERS, 0.5F, 1.0F);
            }

            if (f >= 0.5F && soundEvent2 != null && !this.loaded) {
                this.loaded = true;
                world.playSound((Player) null, user.getX(), user.getY(), user.getZ(), soundEvent2, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }

    }

    private SoundEvent getQuickChargeSound(int stage) {
        return switch (stage) {
            case 1 -> SoundEvents.CROSSBOW_QUICK_CHARGE_1;
            case 2 -> SoundEvents.CROSSBOW_QUICK_CHARGE_2;
            case 3 -> SoundEvents.CROSSBOW_QUICK_CHARGE_3;
            default -> SoundEvents.CROSSBOW_LOADING_START;
        };
    }
}
