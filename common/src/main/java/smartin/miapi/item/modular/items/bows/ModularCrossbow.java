package smartin.miapi.item.modular.items.bows;


import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RepairPriority;
import smartin.miapi.modules.properties.attributes.AttributeUtil;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.projectile.DrawTimeProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    @Environment(EnvType.CLIENT)
    public void registerAnimations() {
        ModularModelPredicateProvider.registerModelOverride(this, ResourceLocation.parse("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                int i = this.getUseDuration(stack, entity) - entity.getTicksUsingItem();
                return entity.getUseItem() != stack ? 0.0F : -getPowerForTime(i, stack, entity);
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
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    public static Collection<ItemStack> getProjectiles(ItemStack stack) {
        return new ArrayList<>();
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemstackReferenceProvider.getFakeReference(this);
        if (itemStack != null) {
            return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
        }
        return 15;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack crossbow = player.getItemInHand(usedHand);
        ChargedProjectiles chargedProjectiles = crossbow.get(DataComponents.CHARGED_PROJECTILES);
        if (chargedProjectiles != null && !chargedProjectiles.isEmpty()) {
            float divergence = (float) Math.pow(12.0, -AttributeUtil.getActualValue(crossbow, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY.value()));
            float speed = (float) Math.max(0.1, AttributeUtil.getActualValue(crossbow, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED.value()) + getShootingPower(chargedProjectiles));
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_PRE_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }
            this.performShooting(level, player, usedHand, crossbow, speed, divergence, null);
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_SHOT.invoker().shoot(player, crossbow).interruptsFurtherEvaluation()) {
                return InteractionResultHolder.consume(crossbow);
            }
            return InteractionResultHolder.consume(crossbow);
        } else if (!player.getProjectile(crossbow).isEmpty()) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
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
            tryLoadProjectiles(livingEntity, stack)) {
            if (MiapiProjectileEvents.MODULAR_CROSSBOW_POST_LOAD.invoker().load(context).interruptsFurtherEvaluation()) {
                return;
            }
            ChargingSounds chargingSounds = this.getChargingSounds(stack);
            chargingSounds.end().ifPresent((holder) -> {
                level.playSound((Player) null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), holder.value(), livingEntity.getSoundSource(), 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
            });
        }
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ChargedProjectiles chargedProjectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
        LoreProperty.appendLoreTop(stack, tooltipComponents, context, tooltipFlag);
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
    }

    private static float getShootingPower(ChargedProjectiles projectile) {
        return projectile.contains(Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }
}
