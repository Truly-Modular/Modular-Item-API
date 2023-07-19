package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.mixin.LootContextTypesAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;
import java.util.function.Supplier;

public class PotionEffectProperty extends CodecBasedProperty<List<PotionEffectProperty.StatusEffectData>> {
    public static LootContextType LOOT_CONTEXT =
            LootContextTypesAccessor.register("miapi:loot_context", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL));
    public static String KEY = "applyPotionEffects";
    public static PotionEffectProperty property;

    public PotionEffectProperty() {
        super(KEY);

        property = this;

        ApplicationEvents.ENTITY_RELATED.startListening(
                (event, entity, stack, data, originals) -> onEntityEvent(event, stack, entity, (List<StatusEffectData>) data, originals),
                ApplicationEvents.StackGetterHolder.ofMulti(
                        property::get,
                        list -> list.stream().map(StatusEffectData::item).toList(),
                        (list, target) -> list.stream().filter(d -> d.item.equals(target)).toList()
                )
        );
    }

    public void onEntityEvent(ApplicationEvent<?, ?, ?> event, ItemStack stack, Entity entity, List<StatusEffectData> effects, Object... originals) {
        if (!(entity.getWorld() instanceof ServerWorld world)) return;
        LootManager predicateManager = entity.getServer() == null ? null : entity.getServer().getLootManager();
        Map<String, Entity> validEntities = new HashMap<>();
        validEntities.put("this", entity);
        if (event instanceof ApplicationEvents.HurtEvent) {
            DamageSource damageSource = (DamageSource) originals[1];
            LivingEntity victim = (LivingEntity) originals[0];

            validEntities.put("victim", victim);
            if (damageSource != null) {
                if (damageSource.getAttacker() != null) validEntities.put("attacker", damageSource.getAttacker());
                if (damageSource.getSource() != null) validEntities.put("source", damageSource.getSource());
            }
        }
        for (StatusEffectData effect : effects) {
            if (!effect.event.equals(event)) continue;

            Entity target = ApplicationEvents.getEntityForTarget(effect.applyTo, validEntities, entity);
            if (!(target instanceof LivingEntity living)) continue;

            if (predicateManager == null || effect.predicate.isEmpty()) {
                StatusEffectInstance instance = effect.creator.get();
                living.addStatusEffect(instance, null);
            } else {
                LootCondition condition = predicateManager.getElement(LootDataType.PREDICATES, effect.predicate.get());
                if (condition != null) {
                    LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                            .add(LootContextParameters.THIS_ENTITY, living) // THIS_ENTITY is whomever the effect is applied to
                            .add(LootContextParameters.ORIGIN, living.getPos())
                            .add(LootContextParameters.TOOL, stack);
                    if (condition.test(new LootContext.Builder(builder.build(LOOT_CONTEXT)).build(null)))
                        living.addStatusEffect(effect.creator.get());
                } else
                    Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
            }
        }
    }

    /*public static void onAbility(AppEventOld<PropAppOld.Ability> event, PropAppOld.Ability ability) {
        if (ability.world().isClient) return;

        List<PotionEffectProperty.StatusEffectData> potionEffects = property.get(ability.stack());
        LootManager manager = ability.user().getServer() == null ? null : ability.user().getServer().getLootManager();
        if (potionEffects != null) {
            for (StatusEffectData effect : potionEffects) {
                if (!effect.event.equals(event)) continue;

                if (effect.ability.isPresent() && !effect.ability.get().equals(ability.ability())) continue;
                if (ability.remainingUseTicks() != null && effect.time.isPresent() && !effect.time.get().test(ability.useTime()))
                    continue;

                if (manager == null || effect.predicate.isEmpty() || !(ability.world() instanceof ServerWorld world))
                    ability.user().addStatusEffect(effect.creator.get());
                else {
                    LootCondition condition = manager.getElement(LootDataType.PREDICATES, effect.predicate.get());
                    if (condition != null) {
                        LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                                .add(LootContextParameters.THIS_ENTITY, ability.user()) // THIS_ENTITY is whomever the effect is applied to
                                .add(LootContextParameters.ORIGIN, ability.user().getPos())
                                .add(LootContextParameters.TOOL, ability.stack());
                        if (condition.test(new LootContext.Builder(builder.build(PropAppOld.Ability.LOOT_CONTEXT)).build(null)))
                            ability.user().addStatusEffect(effect.creator.get());
                    } else
                        Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
                }
            }
        }
    }*/

    @Override
    public Codec<List<StatusEffectData>> codec(ItemModule.ModuleInstance instance) {
        return StatusEffectData.CODEC(new ItemModule.ModuleInstance(ItemModule.empty)).listOf();
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case SMART, EXTEND -> {
                JsonElement element = old.deepCopy();
                element.getAsJsonArray().addAll(toMerge.getAsJsonArray());
                return element;
            }
            case OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    public record StatusEffectData(ApplicationEvent<?, ?, ?> event, String item, String applyTo,
                                   Supplier<StatusEffectInstance> creator, StatusEffect effect, int duration,
                                   int amplifier, boolean ambient, boolean visible, boolean showIcon,
                                   Optional<Identifier> predicate,
                                   Optional<ItemUseAbility> ability, Optional<IntegerRange> time) {
        public static Codec<StatusEffectData> CODEC(ItemModule.ModuleInstance instance) {
            return RecordCodecBuilder.create(inst -> inst.group(
                    ApplicationEvent.codec.fieldOf("event").forGetter(i -> i.event),
                    Codec.STRING.fieldOf("item").forGetter(i -> i.item),
                    Codec.STRING.optionalFieldOf("applyTo", "this").forGetter(i -> i.applyTo),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("duration").forGetter(i -> i.duration),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("amplifier").forGetter(i -> i.amplifier),
                    Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(i -> i.effect), // 1.19.3+ this turns into BuiltinRegistries.STATUS_EFFECT...
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter(i -> i.ambient),
                    Codec.BOOL.optionalFieldOf("visible", true).forGetter(i -> i.visible),
                    Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(i -> i.showIcon),
                    Identifier.CODEC.optionalFieldOf("predicate").forGetter(i -> i.predicate), // allow loot table predicates. ENTITY context is provided (see LootContextTypes.ENTITY)
                    Codec.pair(Codec.STRING.optionalFieldOf("name").codec(), IntegerRange.CODEC.optionalFieldOf("useTime").codec()).optionalFieldOf("ability").forGetter(i -> Optional.of(new Pair<>(i.ability.map(ItemAbilityManager.useAbilityRegistry::findKey), i.time)))
            ).apply(inst, (event, target, applyTo, dur, amp, eff, am, vis, icon, predicate, ability) -> {
                Optional<String> name = Optional.empty();
                Optional<IntegerRange> time = Optional.empty();
                if (ability.isPresent()) {
                    name = ability.get().getFirst();
                    time = ability.get().getSecond();
                }
                return StatusEffectData.create(event, target, applyTo, dur, amp, eff, am, vis, icon, predicate, name, time);
            }));
        }

        public static StatusEffectData create(ApplicationEvent<?, ?, ?> applyEvent, String applyItem, String applyTo, int duration, int amplifier, StatusEffect effect, boolean ambient, boolean visible, boolean showIcon, Optional<Identifier> predicateLocation, Optional<String> abilityName, Optional<IntegerRange> time) {
            return new StatusEffectData(
                    applyEvent,
                    applyItem,
                    applyTo,
                    () -> new StatusEffectInstance(effect, duration, amplifier, ambient, visible, showIcon),
                    effect,
                    duration,
                    amplifier,
                    ambient,
                    visible,
                    showIcon,
                    predicateLocation,
                    abilityName.map(ItemAbilityManager.useAbilityRegistry::get),
                    time
            );
        }
    }

    public record IntegerRange(int min, Optional<Integer> max) {
        public static Codec<IntegerRange> CODEC = Codec.either(Codec.pair(Codec.INT.fieldOf("min").codec(), Codec.INT.optionalFieldOf("max").codec()), Codec.INT)
                .xmap(either -> {
                    if (either.left().isPresent()) {
                        Pair<Integer, Optional<Integer>> ints = either.left().get();
                        return new IntegerRange(ints.getFirst(), ints.getSecond());
                    } else
                        return new IntegerRange(either.right().get(), Optional.empty());
                }, range -> Either.left(new Pair<>(range.min, range.max)));

        public boolean test(int number) {
            return number > min && (max.isEmpty() || number < max.get());
        }
    }
}
