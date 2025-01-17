package smartin.miapi.modules.abilities.block;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.EntityAttributeAbility;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.BlockProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ParryBlock extends EntityAttributeAbility<ParryBlock.ParryContext> {
    public static String KEY = "parry_block";
    UUID attributeUUID = UUID.fromString("4e91990e-4774-11ee-be67-0242ac120002");

    public ParryBlock() {
        LoreProperty.bottomLoreSuppliers.add(itemStack -> {
            List<Text> texts = new ArrayList<>();
            if (AbilityMangerProperty.isPrimaryAbility(this, itemStack)) {
                Text raw = Text.translatable("miapi.ability.parry_block.lore");
                texts.add(raw);
            }
            return texts;
        });
        MiapiEvents.LIVING_HURT.register(new MiapiEvents.LivingHurt() {
            @Override
            public EventResult hurt(MiapiEvents.LivingHurtEvent event) {
                if (event.defender instanceof PlayerEntity defender) {
                    ItemStack activeItem = defender.getActiveItem();
                    if (activeItem != null && ItemAbilityManager.getAbility(activeItem) instanceof ParryBlock) {
                        ParryContext parryContext = getContext(activeItem);
                        if (parryContext != null) {
                            defender.getItemCooldownManager().set(activeItem.getItem(), parryContext.cooldown.evaluate(parryContext.moduleInstance));
                            if (event.damageSource.getAttacker() instanceof LivingEntity attacker) {
                                int cd = (int) (parryContext.cooldownAttackerWeapon.evaluate(parryContext.moduleInstance) + InflictCooldownBlockingProperty.property.getValueSafe(activeItem));
                                if (event.damageSource.getAttacker() instanceof PlayerEntity playerEntity && attacker.getMainHandStack() != null && !attacker.getMainHandStack().isEmpty()) {
                                    playerEntity.getItemCooldownManager().set(
                                            attacker.getMainHandStack().getItem(),
                                            cd);
                                } else {
                                    StatusEffectInstance instance = new StatusEffectInstance(RegistryInventory.stunEffect, cd);
                                    attacker.addStatusEffect(instance);
                                }
                                double returnPercent = (ReflectDamageBlockingProperty.property.getValueSafe(activeItem) + parryContext.damageReturnPercent.evaluate(parryContext.moduleInstance).floatValue()) / 100.0;
                                attacker.damage(
                                        defender.getDamageSources().playerAttack(defender),
                                        (float) (event.amount * returnPercent));
                            }
                            Registries.SOUND_EVENT.getOrEmpty(parryContext.sound).ifPresent((sound -> {
                                defender.playSound(sound,
                                        parryContext.volume.evaluate(parryContext.moduleInstance).floatValue(),
                                        parryContext.pitch.evaluate(parryContext.moduleInstance).floatValue());
                                //defender.playSound(sound, 1.0f, 1.0f);
                            }));
                            return EventResult.interruptDefault();
                        }
                    }
                }
                return EventResult.pass();
            }
        });
    }

    @Override
    protected Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack) {
        Multimap<EntityAttribute, EntityAttributeModifier> multimap = ArrayListMultimap.create();
        double value = BlockProperty.property.getValueSafe(itemStack);
        value = calculate(value);
        //multimap.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(attributeUUID, "miapi-block", -(value / 2) / 100, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        //multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new EntityAttributeModifier(attributeUUID, "miapi-block", value, EntityAttributeModifier.Operation.ADDITION));
        return multimap;
    }

    public static double calculate(double value) {
        return (160.0 / (1 + Math.exp(-value / 50.0))) - 80.0;
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return true;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        ParryContext context = getContext(itemStack);
        if (context != null) {
            return context.maxHoldTime.evaluate(context.moduleInstance) + (int) MaxHoldBlockingProperty.property.getValueSafe(itemStack);
        }
        return 0;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        setAnimation(user, hand);
        return super.use(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        resetAnimation(user);
        ParryContext parryContext = getContext(stack);
        if (parryContext != null && user instanceof PlayerEntity player) {
            player.getItemCooldownManager().set(stack.getItem(), parryContext.cooldownMissTime.evaluate(parryContext.moduleInstance));
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        resetAnimation(user);
        super.onStoppedUsingAfter(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        resetAnimation(user);
        super.onStoppedHolding(stack, world, user);
    }

    public void setAnimation(PlayerEntity p, Hand hand) {
        if (p instanceof ServerPlayerEntity player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            ItemStack itemStack = p.getStackInHand(hand);
            ParryContext context = getContext(itemStack);
            if (facet != null && context != null) {
                facet.set(context.poseId.toString(), player, hand);
            }
        }
    }

    public void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null)
                facet.reset(player);
        }
    }

    @Nullable
    public ParryContext getContext(ItemStack itemStack) {
        var asd = getAbilityContext(itemStack);
        JsonObject object = asd.contextJson;
        if (object != null) {
            ParryContext context = fromJson(object);
            context.moduleInstance = asd.contextInstance;
            return fromJson(object);
        }
        return null;
    }

    public ParryContext fromJson(JsonObject jsonObject) {
        Codec<ParryContext> PARRY_CONTEXT_CODEC = AutoCodec.of(ParryContext.class).codec();
        return PARRY_CONTEXT_CODEC.decode(JsonOps.INSTANCE, jsonObject).result().get().getFirst();
    }

    public static class ParryContext {
        @AutoCodec.Name("pose_id")
        @CodecBehavior.Optional
        public Identifier poseId = new Identifier(Miapi.MOD_ID, "medium_shield_block");
        @AutoCodec.Name("sound")
        @CodecBehavior.Optional
        public Identifier sound = new Identifier("minecraft:block.iron_trapdoor.close");
        @AutoCodec.Name("sound_pitch")
        @CodecBehavior.Optional
        public StatResolver.DoubleFromStat pitch = new StatResolver.DoubleFromStat(1.0);
        @AutoCodec.Name("sound_volume")
        @CodecBehavior.Optional
        public StatResolver.DoubleFromStat volume = new StatResolver.DoubleFromStat(1.0);
        @AutoCodec.Name("max_hold_time")
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat maxHoldTime = new StatResolver.IntegerFromStat(20);
        @AutoCodec.Name("damage_return_percent")
        @CodecBehavior.Optional
        public StatResolver.DoubleFromStat damageReturnPercent = new StatResolver.DoubleFromStat(0.0);
        @AutoCodec.Name("cooldown_attacker_weapon")
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat cooldownAttackerWeapon = new StatResolver.IntegerFromStat(0);
        @AutoCodec.Name("cooldown")
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat cooldown = new StatResolver.IntegerFromStat(40);
        @AutoCodec.Name("cooldown_miss_time")
        @CodecBehavior.Optional
        public StatResolver.IntegerFromStat cooldownMissTime = new StatResolver.IntegerFromStat(40);
        @AutoCodec.Ignored
        public ItemModule.ModuleInstance moduleInstance;

    }
}