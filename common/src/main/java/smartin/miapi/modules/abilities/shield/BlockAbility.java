package smartin.miapi.modules.abilities.shield;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.AbilityMangerProperty;
import smartin.miapi.modules.abilities.util.EntityAttributeAbility;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability is a lesser form of the Block of a Shield.
 * transforms the Value of {@link BlockAbility#calculate(double)} to the actual damage resistance and slowdown percentages
 */
public class BlockAbility extends EntityAttributeAbility<BlockData> {
    ResourceLocation id = Miapi.id("block_ability_temporary_attribute");

    public BlockAbility() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Component> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                Component raw = Component.translatable("miapi.ability.block.lore");
                texts.add(raw);
            }
            return texts;
        });
        MiapiEvents.LIVING_HURT.register(event -> {
            if (!(event.defender instanceof Player player)) return EventResult.pass();
            ItemStack stack = player.getUseItem();
            if (stack == null || stack.isEmpty()) return EventResult.pass();

            ModuleInstance moduleInstance = ItemModule.getModules(stack);
            BlockData data = getData(stack).orElse(null);
            if (data == null || moduleInstance == null) return EventResult.pass();

            player.getCooldowns().addCooldown(stack.getItem(), getCooldown(stack));

            if (event.damageSource.getEntity() instanceof LivingEntity attacker) {
                int attackerCD = (int) data.cooldownAttackerWeapon.getValue();
                if (attacker instanceof Player p) {
                    ItemStack attackStack = p.getMainHandItem();
                    if (!attackStack.isEmpty()) {
                        p.getCooldowns().addCooldown(attackStack.getItem(), attackerCD);
                    }
                } else {
                    // Apply a stun effect

                    attacker.addEffect(new MobEffectInstance(RegistryInventory.stunEffect, attackerCD));
                }

                float returnPercent = (float) data.damageReturnPercent.getValue() / 100f;
                float reflected = event.amount * returnPercent;

                attacker.hurt(player.damageSources().playerAttack(player), reflected);
            }

            // Play sound
            if (data.sound != null) {
                Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(data.sound));
                player.playSound(holder.value(), (float) data.volume.getValue(), (float) data.pitch.getValue());
            }

            return EventResult.pass();
        });
    }

    @Override
    protected Multimap<Holder<Attribute>, AttributeModifier> getAttributes(ItemStack itemStack) {
        Multimap<Holder<Attribute>, AttributeModifier> multimap = ArrayListMultimap.create();
        double value = getData(itemStack).map(c -> c.blocking.getValue()).orElse(1.0).doubleValue();
        value = calculate(value);
        multimap.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(id, -(value / 2) / 100, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
        return multimap;
    }

    public static double calculate(double value) {
        return (160.0 / (1 + Math.exp(-value / 50.0))) - 80.0;
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        setAnimation(user, hand);
        return super.use(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, Level world, LivingEntity user) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user);
    }

    public void setAnimation(Player p, InteractionHand hand) {
        if (p instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.set("miapi:block", player, hand);
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
}