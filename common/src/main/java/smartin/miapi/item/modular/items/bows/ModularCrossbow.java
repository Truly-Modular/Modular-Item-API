package smartin.miapi.item.modular.items.bows;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.entity.ProjectileWithBow;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.FakeItemManager;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.mixin.item.CrossbowItemAccessor;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RepairPriority;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.projectile.DrawTimeProperty;
import smartin.miapi.modules.properties.projectile.IsCrossbowShootAble;
import smartin.miapi.modules.properties.projectile.ShotVelocityOffsetProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.List;
import java.util.function.Predicate;

@NonnullDefault
public class ModularCrossbow extends CrossbowItem implements PlatformModularItemMethods, ModularItem {


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

    @Override
    public ItemStack getDefaultInstance() {
        return FakeItemManager.getDefaultInstance(this);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        ComponentApplyProperty.initializeItemStack(stack, Miapi.registryAccess);
        super.verifyComponentsAfterLoad(stack);
    }

    @Environment(EnvType.CLIENT)
    public void registerAnimations() {
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                int i = entity.getTicksUsingItem();
                return entity.getUseItem() != stack ? 0.0F : getPowerForTime(i, stack, entity);
            }
        });
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        });
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("charged"), (stack, world, entity, seed) -> {
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MiapiEvents.INVENTORY_TICK.invoker().tick(stack, level, entity, slotId, isSelected);
        super.inventoryTick(stack, level, entity, slotId, isSelected);
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
        ItemStack itemStack = FakeItemManager.getLastInstance(this);
        if (itemStack != null) {
            return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
        }
        return 15;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (IsCrossbowShootAble.canCrossbowShoot(ammo) &&
            ammo.getItem() instanceof ProjectileItem projectileItem &&
            !(ammo.getItem() instanceof ArrowItem) &&
            ModularItem.isModularItem(ammo)
        ) {
            Projectile projectile = projectileItem.asProjectile(level, shooter.position(), ammo, shooter.getDirection());
            if (projectile instanceof ItemProjectileEntity projectileEntity) {
                projectileEntity.setCritArrow(isCrit);
                projectile.setPos(shooter.getEyePosition());
                ((ProjectileWithBow) projectile).setBowItem(weapon);
                return projectile;
            }
        }
        if (IsCrossbowShootAble.canCrossbowShoot(ammo) && ammo.getItem() instanceof ProjectileItem projectileItem) {
            Projectile projectile = super.createProjectile(level, shooter, weapon, ammo, isCrit);
            if (projectile instanceof ItemProjectileEntity projectileEntity) {
                projectileEntity.setCritArrow(isCrit);
            }
            ((ProjectileWithBow) projectile).setBowItem(weapon);
            return projectile;
        }
        Projectile projectile1 = super.createProjectile(level, shooter, weapon, ammo, isCrit);
        ((ProjectileWithBow) projectile1).setBowItem(weapon);
        return projectile1;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {
        if (index != 0 && projectile instanceof ItemProjectileEntity entity) {
            entity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        super.shootProjectile(shooter, projectile, index, velocity, inaccuracy, angle, target);
    }

    public static List<ItemStack> drawPublic(ItemStack weapon, ItemStack ammo, LivingEntity shooter) {
        return draw(weapon, ammo, shooter);
    }


    //TODO: somehow give apoth enchants a callback here, maybe custom event?
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack crossbow = player.getItemInHand(usedHand);
        ChargedProjectiles chargedProjectiles = crossbow.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            float divergence = (float) Math.pow(12.0, -AttributeUtil.getActualValue(crossbow, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY.value()));
            float speed = (float) Math.max(0.1, AttributeUtil.getActualValue(crossbow, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()) + getShootingPower(chargedProjectiles));
            double offset = ShotVelocityOffsetProperty.property.getData(chargedProjectiles.getItems().getFirst()).map(DoubleOperationResolvable::getValue).orElse(0.0);
            speed = (float) Math.max(0.1, speed + offset);

            if (MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }
            this.performShooting(level, player, usedHand, crossbow, speed, divergence, null);
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }
            return InteractionResultHolder.consume(crossbow);
        } else if (!player.getProjectile(crossbow).isEmpty()) {
            ((CrossbowItemAccessor) this).setStartSoundPlayed(false);
            ((CrossbowItemAccessor) this).setMidLoadSoundPlayed(false);
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(crossbow);
        } else {
            return InteractionResultHolder.fail(crossbow);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        int ticks = this.getUseDuration(stack, livingEntity) - timeCharged;
        float charge = getPowerForTime(ticks, stack, livingEntity);
        MiapiProjectileEvents.CrossbowLoadingContext context =
                new MiapiProjectileEvents.CrossbowLoadingContext(livingEntity, stack, livingEntity.getProjectile(stack), EquipmentSlot.MAINHAND);
        if (charge >= 1.0F &&
            !isCharged(stack) &&
            !MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_LOAD.invoker().load(context).interruptsFurtherEvaluation() &&
            CrossbowItemAccessor.callTryLoadProjectiles(livingEntity, stack)) {
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_LOAD.invoker().load(context).interruptsFurtherEvaluation()) {
                return;
            }
            ChargingSounds chargingSounds = ((CrossbowItemAccessor) this).callGetChargingSounds(stack);
            chargingSounds.end().ifPresent((holder) -> {
                level.playSound((Player) null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), holder.value(), livingEntity.getSoundSource(), 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
            });
        }
    }


    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return super.getAllSupportedProjectiles().or(IsCrossbowShootAble::canCrossbowShoot);
    }


    private static float getPowerForTime(int timeLeft, ItemStack stack, LivingEntity shooter) {
        float f = (float) timeLeft / (float) getChargeDuration(stack, shooter);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public static int getChargeDuration(ItemStack stack, LivingEntity shooter) {
        double drawTime = DrawTimeProperty.property.getValue(stack).orElse(0.25);
        float f = EnchantmentHelper.modifyCrossbowChargingTime(stack, shooter, (float) drawTime);
        return Mth.floor(f * 20.0F);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getChargeDuration(stack, entity) + 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        /*
        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            ItemStack itemStack = chargedProjectiles.getItems().getFirst();
            tooltipComponents.add(Component.translatable("item.minecraft.crossbow.projectile").append(CommonComponents.SPACE).append(itemStack.getDisplayName()));
            if (tooltipFlag.isAdvanced() && itemStack.is(Items.FIREWORK_ROCKET)) {
                List<Component> list = Lists.newArrayList();
                Items.FIREWORK_ROCKET.appendHoverText(itemStack, context, list, tooltipFlag);
                if (!list.isEmpty()) {
                    for (int i = 0; i < list.size(); ++i) {
                        list.set(i, Component.literal("  ").append(list.get(i)).withStyle(ChatFormatting.GRAY));
                    }

                    tooltipComponents.addAll(list);
                }
            }
        }
         */
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        LoreProperty.appendLoreTop(stack, tooltipComponents, context, tooltipFlag);
    }

    private static float getShootingPower(ChargedProjectiles projectile) {
        return projectile.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }
}
