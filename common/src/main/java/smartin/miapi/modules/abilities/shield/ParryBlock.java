package smartin.miapi.modules.abilities.shield;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableInt;
import smartin.miapi.client.gui.crafting.statdisplay.DoubleResolvableStatDisplay;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.events.ClientEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.mixin.CooldownInstanceAccessor;
import smartin.miapi.mixin.ItemCooldownsAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.MinMaxCDAbility;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

import static smartin.miapi.attributes.AttributeRegistry.SHIELD_BREAK;
import static smartin.miapi.events.MiapiEvents.GET_ITEM_SHIELD_COOLDOWN;

public class ParryBlock extends MinMaxCDAbility<BlockData> {
    public static final String KEY = "parry_block";
    public static final MapCodec<BlockData> CODEC = AutoCodec.of(BlockData.class);

    public ParryBlock() {
        super(0, 7200, 30);

        GET_ITEM_SHIELD_COOLDOWN.register(new MiapiEvents.CooldownAttackingWeaponGatherEvent() {
            @Override
            public void durability(MutableInt cooldown, ItemStack attacking, ItemStack shield, LivingEntity defender, Entity attacker) {
                if (ModularItem.isModularItem(attacking) && attacker instanceof LivingEntity livingEntity) {
                    double value = livingEntity.getAttributeValue(SHIELD_BREAK);
                    cooldown.setValue(value * 20);
                }
                if (cooldown.getValue() == 0 && attacking.getItem() instanceof AxeItem) {
                    cooldown.setValue(100);
                }
            }
        }, -1);

        MiapiEvents.LIVING_HURT.register(event -> {
            if (!(event.defender instanceof Player player)) return EventResult.pass();
            ItemStack stack = player.getUseItem();
            if (stack == null || stack.isEmpty()) return EventResult.pass();

            ModuleInstance moduleInstance = ItemModule.getModules(stack);
            BlockData data = getData(stack).orElse(null);
            if (data == null || moduleInstance == null || event.attacker == null) return EventResult.pass();

            double allowedAngle = data.angle().getValue();

            // Player look direction
            Vec3 playerLook = player.getLookAngle().normalize();

            // Direction from player to attacker
            Vec3 toAttacker = event.attacker.position().subtract(player.position()).normalize();

            // Calculate angle between the vectors (in degrees)
            double angle = Math.toDegrees(Math.acos(playerLook.dot(toAttacker)));

            if (angle <= allowedAngle) {
                // Direction from player to attacker
                toAttacker = event.attacker.getEyePosition().subtract(player.position()).normalize();

                // Calculate angle between the vectors (in degrees)
                angle = Math.toDegrees(Math.acos(playerLook.dot(toAttacker)));
                if (angle <= allowedAngle) {
                    return EventResult.interruptTrue();
                }
            }


            if (event.damageSource.getEntity() instanceof LivingEntity attacker) {
                int attackerCD = (int) data.cooldownAttackerWeapon().getValue();
                if (attacker instanceof Player p) {
                    ItemStack attackStack = p.getMainHandItem();
                    if (!attackStack.isEmpty()) {
                        addCooldown(p, attackStack, attackerCD);
                    }
                } else {
                    // Apply a stun effect
                    if (attackerCD > 10) {
                        attacker.addEffect(new MobEffectInstance(RegistryInventory.stunEffect, attackerCD));
                    }
                }

                float returnPercent = (float) data.damageReturnPercent().getValue() / 100f;
                if (returnPercent > 1) {
                    float reflected = event.amount * returnPercent;
                    attacker.hurt(player.damageSources().playerAttack(player), reflected);
                }
            }

            // Play sound
            if (data.sound() != null) {
                SoundEvent soundEvent= BuiltInRegistries.SOUND_EVENT.get(data.sound());
                if(soundEvent!=null){
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.level().playSound(
                                player,
                                player.getOnPos(),
                                soundEvent,
                                SoundSource.PLAYERS,
                                (float) data.volume().getValue(),
                                (float) data.pitch().getValue());
                    }
                }
            }
            double blocking = data.blocking().getValue();
            int cooldown = getCooldown(stack);
            if (data.respectAttackingWeaponCooldown().getValue() > 0) {
                MutableInt base = new MutableInt(0);
                GET_ITEM_SHIELD_COOLDOWN.invoker().durability(base, event.getMainCausingStack(), stack, event.defender, event.attacker);
                cooldown += base.getValue();
            }
            if (blocking >= 100) {
                addCooldown(player, stack, cooldown);
                return EventResult.interruptDefault();
            }
            if (blocking > 0) {
                float blockPercent = (float) blocking / 100f;
                event.amount = Math.max(0, event.amount - blockPercent * event.amount);
                addCooldown(player, stack, cooldown);
                return EventResult.pass();
            }
            return EventResult.pass();
        });
        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientEvents.STAT_WIDGET_REGISTRATION.register(this::registerStatDisplays);
        }
    }

    @Environment(EnvType.CLIENT)
    public void registerStatDisplays() {
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).blocking())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.block"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.block.description"))
                        .build());

        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((MinMaxCDData<?>) a.context()).max())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.max_use"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.max_use.description"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((MinMaxCDData<?>) a.context()).min())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.min_use"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.min_use.description"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).respectAttackingWeaponCooldown())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.respect_attacker_weapon_cd"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.respect_attacker_weapon_cd"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).angle())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.angle"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.angle"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).cooldownAttackerWeapon())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.cooldown_attacker_weapon"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.cooldown_attacker_weapon.description"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).damageReturnPercent())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.damage_return_percent"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.damage_return_percent.description"))
                        .build());
        StatListWidget.addStatDisplay(
                DoubleResolvableStatDisplay
                        .builder(
                                (s -> ItemAbilityManager.getAbilities(s)
                                        .stream()
                                        .filter(a -> a.ability() instanceof ParryBlock)
                                        .findAny().map(a -> ((BlockData) ((MinMaxCDData<?>) a.context()).data()).cooldownMissTime())
                                ))
                        .setName(Component.translatable("miapi.stat.miapi.ability.blocking.cooldown_miss_time"))
                        .setHoverDescription(Component.translatable("miapi.stat.miapi.ability.blocking.cooldown_miss_time.description"))
                        .build());
    }

    public void addCooldown(Player player, ItemStack stack, int cooldown) {
        if (cooldown > 0) {

            Map<Item, ItemCooldowns.CooldownInstance> cd = ((ItemCooldownsAccessor) player.getCooldowns()).getCooldowns();
            if (cd.containsKey(stack.getItem())) {
                int currentCD = ((CooldownInstanceAccessor) cd.get(stack.getItem())).getEndTime() - ((ItemCooldownsAccessor) player.getCooldowns()).getTickCount();
                if (currentCD > cooldown) {
                    return;
                }
            }
            player.getCooldowns().addCooldown(stack.getItem(), cooldown);
            player.stopUsingItem();
            player.getCooldowns().addCooldown(stack.getItem(), cooldown);
        }
    }


    @Override
    protected MapCodec<BlockData> getMapCodec() {
        return BlockData.CODEC;
    }

    @Override
    protected BlockData mergeData(BlockData left, BlockData right, MergeType mergeType) {
        return left.merge(left, right, mergeType);
    }

    @Override
    public BlockData initializeData(BlockData data, ModuleInstance moduleInstance) {
        return data.initialize(data, moduleInstance);
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext, MinMaxCDData<BlockData> context) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, MinMaxCDData<BlockData> context) {
        if (getData(itemStack).isPresent()) {
            var data = getData(itemStack).get();
            if (data.pose().isEmpty()) {
                return UseAnim.BLOCK;
            }
        }
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, MinMaxCDData<BlockData> context) {
        if (!world.isClientSide) {
            if (user instanceof ServerPlayer serverPlayer) {
                ModuleInstance moduleInstance = ItemModule.getModules(user.getItemInHand(hand));
                if (moduleInstance != null) {
                    BlockData data = getData(user.getItemInHand(hand)).orElse(null);
                    if (data != null) {
                        // Pose animation setup
                        if (data.pose() != null && data.pose().isPresent()) {
                            setAnimation(serverPlayer, data.pose().get(), hand);
                        }
                    }
                }
            }
        }

        user.startUsingItem(hand);
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user, MinMaxCDData<BlockData> context) {
        resetAnimation(user);
        applyCooldownMissTime(stack, user);
        return super.finishUsing(stack, world, user, context);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, MinMaxCDData<BlockData> context) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks, context);
        applyCooldownMissTime(stack, user);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user, MinMaxCDData<BlockData> context) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user, context);
        applyCooldownMissTime(stack, user);
    }

    public void applyCooldownMissTime(ItemStack itemStack, LivingEntity livingEntity) {
        BlockData data = getData(itemStack).orElse(null);
        if (livingEntity instanceof ServerPlayer serverPlayer && data != null) {

            addCooldown(serverPlayer, itemStack, (int) data.cooldownMissTime().getValue());
        }
    }

    public void setAnimation(Player p, ResourceLocation id, InteractionHand hand) {
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set(id.toString(), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }
}
