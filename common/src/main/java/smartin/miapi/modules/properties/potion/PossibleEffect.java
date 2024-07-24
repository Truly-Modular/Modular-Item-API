package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * this is a utility class for decoding complex Possible Potion effects
 * it allows for a target boolean, probability, potion effect and equipment slot.
 *
 * @param effect      the Potioneffect given
 * @param probability the probability of the potion effect beeing applied
 * @param targetSelf  the target selector, target or targetSelf
 * @param group       the EquipmentSlot predicate to limit the usability to certain Equipment slots
 */
public record PossibleEffect(MobEffectInstance effect, float probability, boolean targetSelf,
                             EquipmentSlotGroup group) {
    public static final Codec<PossibleEffect> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                MobEffectInstance.CODEC
                        .fieldOf("potion")
                        .forGetter(PossibleEffect::effect),
                Codec.floatRange(0.0F, 1.0F)
                        .optionalFieldOf("probability", 1.0F)
                        .forGetter(PossibleEffect::probability),
                Codec.BOOL
                        .optionalFieldOf("target_self", false)
                        .forGetter(PossibleEffect::targetSelf),
                EquipmentSlotGroup.CODEC
                        .optionalFieldOf("equipment_slot", EquipmentSlotGroup.ANY)
                        .forGetter(PossibleEffect::group)
        ).apply(instance, PossibleEffect::new);
    });

    public void apply(LivingEntity wielder, RandomSource random, EquipmentSlot equipmentSlot, LivingEntity target, LivingEntity selfTarget) {
        if (this.group().test(equipmentSlot)) {
            if (targetSelf()) {
                target = selfTarget;
            }
            if (target != null) {
                if (probability() == 1.0 || random.nextDouble() < probability())
                    target.addEffect(new MobEffectInstance(effect()), wielder);
            }
        }
    }

    public void apply(List<PossibleEffect> effects, LivingEntity wielder, RandomSource random, EquipmentSlot equipmentSlot, LivingEntity target, LivingEntity selfTarget) {
        effects.forEach(effect -> {
            effect.apply(wielder, random, equipmentSlot, target, selfTarget);
        });
    }

    public Component getTooltip(String key, String showTargetSelf) {
        return Component.translatable(targetSelf() ? key : showTargetSelf,
                effect().getEffect().value().getDisplayName(),
                effect().getDuration(),
                effect().getAmplifier(),
                probability() == 1.0 ? Component.empty() : probability()
        );
    }

    public static List<Component> getTooltip(Component header, List<PossibleEffect> effects, String key, String showTargetSelf) {
        List<Component> components = new ArrayList<>();
        if (!effects.isEmpty()) {
            components.add(Component.empty());
            components.add(header);
            for (PossibleEffect effect : effects) {
                components.add(effect.getTooltip(key, showTargetSelf));
            }
        }
        return components;
    }

    public static void applyEffects(LivingEntity target, LivingEntity itemsFromEntity, Function<ItemStack, List<PossibleEffect>> effectGetter) {
        applyEffects(target, target, itemsFromEntity, effectGetter);
    }

    public static void applyEffects(LivingEntity target, LivingEntity selfTarget, LivingEntity itemsFromEntity, Function<ItemStack, List<PossibleEffect>> effectGetter) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            itemsFromEntity.getItemBySlot(slot);
            effectGetter.apply(itemsFromEntity.getItemBySlot(slot)).forEach(possibleEffect -> {
                possibleEffect.apply(itemsFromEntity, itemsFromEntity.level().getRandom(), slot, target, selfTarget);
            });
        }
    }

    public static List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        List<PossibleEffect> mergedList = new ArrayList<>();
        List<PossibleEffect> directMerged = new ArrayList<>(left);
        directMerged.addAll(right);
        directMerged.forEach(possibleEffect -> {
            Optional<PossibleEffect> mergeWith =
                    mergedList.stream().filter(possibleEffect1 ->
                            possibleEffect1.effect().getEffect().equals(possibleEffect.effect().getEffect()) &&
                            possibleEffect1.targetSelf() == possibleEffect.targetSelf() &&
                            possibleEffect1.effect().getAmplifier() == possibleEffect.effect().getAmplifier()
                    ).findFirst();
            if (mergeWith.isPresent()) {
                PossibleEffect first = mergeWith.get();
                mergedList.remove(first);

                float combinedProbability = first.probability() + possibleEffect.probability();
                int combinedDuration = first.effect().getDuration() +
                                       (combinedProbability > 1.0f ? (int) (possibleEffect.effect().getDuration() * (combinedProbability - 1.0f))
                                               : possibleEffect.effect().getDuration());
                combinedProbability = Math.min(combinedProbability, 1.0f);

                MobEffectInstance mergedMobEffect = new MobEffectInstance(
                        first.effect().getEffect(),
                        combinedDuration,
                        first.effect().getAmplifier(),
                        first.effect().isAmbient(),
                        first.effect().isVisible()
                );

                mergedList.add(new PossibleEffect(mergedMobEffect, combinedProbability, first.targetSelf(), first.group()));
            } else {
                mergedList.add(possibleEffect);
            }
        });
        return mergedList;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PossibleEffect> STREAM_CODEC =
            StreamCodec.composite(
                    MobEffectInstance.STREAM_CODEC,
                    PossibleEffect::effect,
                    ByteBufCodecs.FLOAT,
                    PossibleEffect::probability,
                    ByteBufCodecs.BOOL,
                    PossibleEffect::targetSelf,
                    EquipmentSlotGroup.STREAM_CODEC,
                    PossibleEffect::group,
                    PossibleEffect::new);
}
