package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.entity.uslot.SlotSelection;
import smartin.miapi.entity.uslot.UEquipmentSlot;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * this is a utility class for decoding complex Possible Potion effects
 * it allows for a target boolean, probability, potion effect and equipment slot.
 *
 * @param potion      the Potioneffect given
 * @param probability the probability of the potion effect beeing applied
 * @param targetSelf  the target selector, target or targetSelf
 * @param group       the EquipmentSlot predicate to limit the usability to certain Equipment slots
 */
public record PossibleEffect(Holder<MobEffect> potion,
                             DoubleOperationResolvable amplifier,
                             DoubleOperationResolvable duration,
                             DoubleOperationResolvable ambient,
                             DoubleOperationResolvable showParticle,
                             DoubleOperationResolvable showIcon,
                             DoubleOperationResolvable probability,
                             DoubleOperationResolvable targetSelf,
                             SlotSelection group) implements InitializeAble<PossibleEffect> {
    public static final Codec<PossibleEffect> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                MobEffect.CODEC
                        .fieldOf("potion")
                        .forGetter(PossibleEffect::potion),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("amplifier", new DoubleOperationResolvable(0))
                        .forGetter(PossibleEffect::amplifier),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("duration", new DoubleOperationResolvable(0))
                        .forGetter(PossibleEffect::duration),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("ambient", new DoubleOperationResolvable(0))
                        .forGetter(PossibleEffect::ambient),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("show_particles", new DoubleOperationResolvable(1))
                        .forGetter(PossibleEffect::showParticle),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("show_icon", new DoubleOperationResolvable(1))
                        .forGetter(PossibleEffect::showIcon),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("probability", new DoubleOperationResolvable(1))
                        .forGetter(PossibleEffect::showIcon),
                DoubleOperationResolvable.CODEC
                        .optionalFieldOf("target_self", new DoubleOperationResolvable(1))
                        .forGetter(PossibleEffect::showIcon),
                SlotSelection.CODEC
                        .optionalFieldOf("equipment_slot", SlotSelection.ALL)
                        .forGetter(PossibleEffect::group)
        ).apply(instance, PossibleEffect::new);
    });
    //(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible, boolean showIcon

    public MobEffectInstance getEffect() {
        return new MobEffectInstance(potion(), (int) duration().getValue(), (int) amplifier().getValue(), ambient().isTrue(), showParticle().isTrue(), showIcon().isTrue());
    }

    public void apply(LivingEntity wielder, RandomSource random, UEquipmentSlot equipmentSlot, LivingEntity target, LivingEntity selfTarget) {
        if (equipmentSlot == null || this.group().matches(equipmentSlot)) {
            applyNoCheck(wielder, random, target, selfTarget);
        }
    }

    public void applyNoCheck(LivingEntity wielder, RandomSource random, LivingEntity target, LivingEntity selfTarget) {
        if (targetSelf().isTrue()) {
            target = selfTarget;
        }
        if (target != null) {
            if (probability().getValue() == 1.0 || random.nextDouble() < probability().getValue()) {
                target.addEffect(getEffect(), wielder);
            }
        }
    }

    public static void apply(LivingEntity wielder, Function<ItemStack, List<PossibleEffect>> effectGetter, RandomSource random, LivingEntity target, LivingEntity selfTarget) {
        List<PossibleEffect> effects = new ArrayList<>();
        for (UEquipmentSlot.EquipedItem item : SlotSelection.scanItems(wielder)) {
            effects = merge(effects, effectGetter.apply(item.itemStack()).stream().filter(possibleEffect -> possibleEffect.group().matches(item.slot())).toList(), MergeType.SMART);
        }
        effects.forEach(effect -> {
            effect.applyNoCheck(wielder, random, target, selfTarget);
        });
    }

    public Component getTooltip(String key, String showTargetSelf) {
        return Component.translatable(targetSelf().isTrue() ? showTargetSelf : key,
                potion().value().getDisplayName(),
                (int) duration().getValue() / 20,
                (int) amplifier().getValue() + 1,
                probability().getValue() == 1.0 ? Component.empty() : probability()
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

    /**
    use {@link PossibleEffect#apply(LivingEntity, Function, RandomSource, LivingEntity, LivingEntity)} instead!
     */
    @Deprecated
    public static void applyEffects(
            LivingEntity target,
            LivingEntity selfTarget,
            Iterable<ItemStack> items,
            LivingEntity causer,
            Function<ItemStack, List<PossibleEffect>> effectGetter
    ) {
        List<PossibleEffect> mergedEffects = new ArrayList<>();

        for (ItemStack item : items) {
            List<PossibleEffect> itemEffects = effectGetter.apply(item);
            mergedEffects = merge(mergedEffects, itemEffects, MergeType.SMART);
        }

        RandomSource random = causer.level().getRandom();

        for (PossibleEffect effect : mergedEffects) {
            effect.apply(causer, random, null, target, selfTarget);
        }
    }

    public static List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        List<PossibleEffect> mergedList = new ArrayList<>();
        List<PossibleEffect> directMerged = new ArrayList<>(left);
        directMerged.addAll(right);
        directMerged.forEach(possibleEffect -> {
            Optional<PossibleEffect> mergeWith =
                    mergedList.stream().filter(possibleEffect1 ->
                            possibleEffect1.potion().equals(possibleEffect.potion()) &&
                            possibleEffect1.targetSelf().isTrue() == possibleEffect.targetSelf().isTrue() &&
                            (int) possibleEffect1.amplifier().getValue() == (int) possibleEffect.amplifier().getValue()
                    ).findFirst();
            if (mergeWith.isPresent()) {
                PossibleEffect first = mergeWith.get();
                if (!(first.duration.isInitialized() && possibleEffect.duration.isInitialized())) {
                    mergedList.add(new PossibleEffect(
                                    first.potion,
                                    DoubleOperationResolvable.merge(first.amplifier, possibleEffect.amplifier, mergeType),
                                    DoubleOperationResolvable.merge(first.duration, possibleEffect.duration, mergeType),
                                    DoubleOperationResolvable.merge(first.ambient, possibleEffect.ambient, mergeType),
                                    DoubleOperationResolvable.merge(first.showParticle, possibleEffect.showParticle, mergeType),
                                    DoubleOperationResolvable.merge(first.showIcon, possibleEffect.showIcon, mergeType),
                                    DoubleOperationResolvable.merge(first.probability, possibleEffect.probability, mergeType),
                                    first.targetSelf,
                                    first.group()
                            )
                    );
                    mergedList.remove(first);
                } else {
                    mergedList.remove(first);

                    double combinedProbability = first.probability().getValue() + possibleEffect.probability().getValue();
                    int combinedDuration = (int) (first.duration().getValue() +
                                                  (combinedProbability > 1.0f ? (int) (first.duration().getValue() * (combinedProbability - 1.0f))
                                                          : first.duration().getValue()));
                    combinedProbability = Math.min(combinedProbability, 1.0f);
                    mergedList.add(new PossibleEffect(
                                    first.potion,
                                    DoubleOperationResolvable.merge(first.amplifier, possibleEffect.amplifier, mergeType),
                                    new DoubleOperationResolvable(combinedDuration),
                                    DoubleOperationResolvable.merge(first.ambient, possibleEffect.ambient, mergeType),
                                    DoubleOperationResolvable.merge(first.showParticle, possibleEffect.showParticle, mergeType),
                                    DoubleOperationResolvable.merge(first.showIcon, possibleEffect.showIcon, mergeType),
                                    new DoubleOperationResolvable(combinedProbability),
                                    first.targetSelf,
                                    first.group()
                            )
                    );
                }
            } else {
                mergedList.add(possibleEffect);
            }
        });
        return mergedList;
    }

    public PossibleEffect initialize(PossibleEffect original, ModuleInstance moduleInstance) {
        return new PossibleEffect(
                original.potion(),
                original.amplifier().initialize(moduleInstance),
                original.duration().initialize(moduleInstance),
                original.ambient().initialize(moduleInstance),
                original.showParticle().initialize(moduleInstance),
                original.showIcon().initialize(moduleInstance),
                original.probability().initialize(moduleInstance),
                original.targetSelf().initialize(moduleInstance),
                original.group()
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PossibleEffect> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
