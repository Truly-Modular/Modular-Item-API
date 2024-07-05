package smartin.miapi.item.modular.items;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.CustomDrawTimeItem;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.*;
import smartin.miapi.modules.properties.attributes.AttributeProperty;

import java.util.List;
import java.util.function.Predicate;

import static smartin.miapi.item.modular.items.ModularBow.projectile;

public class ModularCrossbow extends CrossbowItem implements PlatformModularItemMethods, ModularItem, CustomDrawTimeItem {
    public ModularCrossbow(Properties settings) {
        super(settings.stacksTo(1).durability(50));
        if (smartin.miapi.Environment.isClient()) {
            registerAnimations();
        }
    }

    public ModularCrossbow() {
        super(new Item.Properties().stacksTo(1).durability(50));
        if (smartin.miapi.Environment.isClient()) {
            registerAnimations();
        }
    }

    @Environment(EnvType.CLIENT)
    public void registerAnimations() {
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getUseItem() != stack ? 0.0F : getPullProgress((stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()), stack);
            }
        });
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        });
        ModularModelPredicateProvider.registerModelOverride(this,ResourceLocation.parse("charged"), (stack, world, entity, seed) -> {
            return entity != null && isCharged(stack) ? 1.0F : 0.0F;
        });
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamageValue()) / ModularItem.getDurability(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        int i = this.getMaxUseTime(stack) - remainingUseTicks;
        float f = getPullProgress(i, stack);
        if (f >= 1.0F && !isCharged(stack) && tryLoadProjectiles(user, stack)) {
            setCharged(stack, true);
            SoundSource soundCategory = user instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundCategory, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    public int getMaxUseTime(ItemStack stack) {
        return getPullTime(stack) + 3;
    }

    private static float getPullProgress(int useTicks, ItemStack stack) {
        float progress = (float) useTicks / (float) getPullTime(stack);
        return Math.max(0, Math.min(1, progress));
    }

    public static int getPullTime(ItemStack stack) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, stack);
        double drawTime = (25 - AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.BOW_DRAW_TIME));
        drawTime = Math.max(5, drawTime - drawTime / 5 * i);
        if (Double.isNaN(drawTime)) return 5;
        return (int) drawTime;
    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
        int j = i == 0 ? 1 : 3;
        boolean isCreative = shooter instanceof Player player && player.getAbilities().instabuild;
        ItemStack itemStack = shooter.getProjectile(crossbow);

        for (int k = 0; k < j; ++k) {
            if (k > 0) {
                itemStack = itemStack.copy();
            }

            if (itemStack.isEmpty() && isCreative) {
                itemStack = new ItemStack(Items.ARROW);
            }
            MiapiProjectileEvents.CrossbowLoadingContext context =
                    new MiapiProjectileEvents.CrossbowLoadingContext(shooter, crossbow, itemStack);


            if (MiapiProjectileEvents.MODULAR_CROSSBOW_LOAD.invoker().load(context).interruptsFurtherEvaluation()) {
                return false;
            }


            if (!loadProjectile(shooter, crossbow, context.loadingProjectile, k > 0, isCreative)) {
                return false;
            }

            if (MiapiProjectileEvents.MODULAR_CROSSBOW_LOAD_AFTER.invoker().load(context).interruptsFurtherEvaluation()) {
                return false;
            }
        }

        return true;
    }

    private static boolean loadProjectile(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative) {
        if (projectile.isEmpty()) {
            return false;
        } else {
            ItemStack itemStack;
            if (!creative && !simulated) {
                itemStack = projectile.split(1);
                if (itemStack.isDamageableItem()) {
                    itemStack.hurtAndBreak(1, shooter, LivingEntity -> {
                    });
                }
            } else {
                itemStack = projectile.copy();
            }

            putProjectile(crossbow, itemStack);
            return true;
        }
    }

    public static void putProjectile(ItemStack crossbow, ItemStack projectile) {
        CompoundTag nbtCompound = crossbow.getOrCreateNbt();
        ListTag nbtList;
        if (nbtCompound.contains("ChargedProjectiles", 9)) {
            nbtList = nbtCompound.getList("ChargedProjectiles", 10);
        } else {
            nbtList = new ListTag();
        }

        CompoundTag nbtCompound2 = new CompoundTag();
        projectile.writeNbt(nbtCompound2);
        nbtList.add(nbtCompound2);
        nbtCompound.put("ChargedProjectiles", nbtList);
        ModularItemCache.clearUUIDFor(crossbow);
    }

    private static void clearProjectiles(ItemStack crossbow) {
        CompoundTag nbtCompound = crossbow.getNbt();
        if (nbtCompound != null) {
            ListTag nbtList = nbtCompound.getList("ChargedProjectiles", 9);
            nbtList.clear();
            nbtCompound.put("ChargedProjectiles", nbtList);
        }
        crossbow.setNbt(nbtCompound);
        ModularItemCache.clearUUIDFor(crossbow);
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
                Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(simulated * 0.017453292F, vec3d.x, vec3d.y, vec3d.z);
                Vec3 vec3d2 = shooter.getViewVector(1.0F);
                Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
                projectileEntity.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
            }

            crossbow.hurtAndBreak(bl ? 3 : 1, shooter, (e) -> {
                e.sendToolBreakStatus(hand);
            });
            if (projectileEntity instanceof AbstractArrow persistentProjectileEntity) {
                MiapiProjectileEvents.ModularBowShotEvent event = new MiapiProjectileEvents.ModularBowShotEvent(persistentProjectileEntity, crossbow, shooter);
                if (MiapiProjectileEvents.MODULAR_BOW_SHOT.invoker().call(event).interruptsFurtherEvaluation()) {
                    return;
                }
            }
            world.addFreshEntity(projectileEntity);
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, soundPitch);
            if (projectileEntity instanceof AbstractArrow persistentProjectileEntity) {
                MiapiProjectileEvents.ModularBowShotEvent event = new MiapiProjectileEvents.ModularBowShotEvent(persistentProjectileEntity, crossbow, shooter);
                MiapiProjectileEvents.MODULAR_BOW_POST_SHOT.invoker().call(event);
            }
        }
    }

    private static AbstractArrow createArrow(Level world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
        AbstractArrow persistentProjectileEntity;
        if (arrow.getItem() instanceof ModularItem && !(arrow.getItem() instanceof ArrowItem)) {
            persistentProjectileEntity = new ItemProjectileEntity(world, entity, arrow);
        } else {
            ArrowItem arrowItem = (ArrowItem) (arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
            persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
            if (entity instanceof Player) {
                persistentProjectileEntity.setCritArrow(true);
            }
        }

        persistentProjectileEntity.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        persistentProjectileEntity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte) ((byte) i + AttributeProperty.getActualValue(arrow, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING)));
        }

        return persistentProjectileEntity;
    }

    public static void shootAll(Level world, LivingEntity entity, InteractionHand hand, ItemStack stack, float speed, float divergence) {
        List<ItemStack> list = getProjectiles(stack);
        float[] fs = getSoundPitches(entity.getRandom());

        for (int i = 0; i < list.size(); ++i) {
            ItemStack itemStack = list.get(i);
            if (itemStack.isDamageableItem()) {

            }
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
        return new float[]{1.0F, getRandomShotPitch(bl, random), getRandomShotPitch(!bl, random)};
    }

    private static void postShoot(Level world, LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayer serverPlayerEntity) {
            if (!world.isClientSide) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }

            serverPlayerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        }

        clearProjectiles(stack);
        setCharged(stack, false);
        ModularItemCache.getUUIDFor(stack);
        MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.invoker().shoot(entity, stack);
    }

    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (isCharged(itemStack)) {
            //firedArrow
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_SHOT.invoker().shoot(user, itemStack).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.fail(itemStack);
            }
            shootAll(world, user, hand, itemStack, getSpeed(itemStack), 1.0F);
            itemStack.hurtAndBreak(1, user, p -> p.sendToolBreakStatus(user.getActiveHand()));
            Miapi.LOGGER.info("do shoot crossbow");
            return InteractionResultHolder.consume(itemStack);
        } else if (!user.getProjectile(itemStack).isEmpty()) {
            if (!isCharged(itemStack)) {
                this.startSoundPlayed = false;
                this.midLoadSoundPlayed = false;
                user.startUsingItem(hand);
            }
            Miapi.LOGGER.info("do load crossbow");
            return InteractionResultHolder.consume(itemStack);
        } else {
            Miapi.LOGGER.info("do nothing crossbow");
            return InteractionResultHolder.fail(itemStack);
        }
    }

    private static float getRandomShotPitch(boolean flag, RandomSource random) {
        float f = flag ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    private static float getSpeed(ItemStack stack) {
        float baseSpeed = hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
        baseSpeed += (float) AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
        return baseSpeed;
    }

    public static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        CompoundTag nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains("ChargedProjectiles", 9)) {
            ListTag nbtList = nbtCompound.getList("ChargedProjectiles", 10);
            if (nbtList != null) {
                for (int i = 0; i < nbtList.size(); ++i) {
                    CompoundTag nbtCompound2 = nbtList.getCompound(i);
                    list.add(ItemStack.parse(nbtCompound2));
                }
            }
        }

        return list;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        List<ItemStack> porjectiles = getProjectiles(stack);
        if (isCharged(stack) && !porjectiles.isEmpty()) {
            ItemStack itemStack = (ItemStack) porjectiles.get(0);
            tooltip.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack.getDisplayName()));
            if (context.isAdvanced() && itemStack.is(Items.FIREWORK_ROCKET)) {
                List<Component> list2 = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemStack, world, list2, context);
                if (!list2.isEmpty()) {
                    for (int i = 0; i < list2.size(); ++i) {
                        list2.set(i, Component.literal("  ").append((Component) list2.get(i)).withStyle(ChatFormatting.GRAY));
                    }

                    tooltip.addAll(list2);
                }
            }

        }
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return itemStack -> projectile.test(itemStack) || IsCrossbowShootAble.canCrossbowShoot(itemStack);
    }

    @Override
    public double getBaseDrawTime(ItemStack itemStack) {
        return 25;
    }
}
