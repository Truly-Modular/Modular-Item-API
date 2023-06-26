package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import smartin.miapi.events.Event;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.PropertyApplication;
import smartin.miapi.modules.properties.util.SimpleEventProperty;

import java.util.*;
import java.util.function.Supplier;

import static smartin.miapi.modules.properties.util.PropertyApplication.ApplicationEvent.*;

public class PotionEffectProperty extends SimpleEventProperty {
    public static String KEY = "applyPotionEffects";
    public static PotionEffectProperty property;

    public PotionEffectProperty() {
        super(
                new EventHandlingMap<>()
                        .set(HURT, PotionEffectProperty::onEntityHurt)
                        .setAll(ABILITIES, PotionEffectProperty::onAbility),
                false, () -> property
        );
        property = this;
        ModularItemCache.setSupplier(KEY, PotionEffectProperty::createCache);
    }

    public static void onEntityHurt(PropertyApplication.Cancellable<Event.LivingHurtEvent> holder) {
        Event.LivingHurtEvent event = holder.event();
        LivingEntity victim = event.livingEntity;
        if (!(victim.getWorld() instanceof ServerWorld world) || !(event.damageSource.getAttacker() instanceof LivingEntity attacker))
            return;
        LootManager predicateManager = victim.getServer() == null ? null : victim.getServer().getLootManager();

        List<StatusEffectData> effects = ofEntity(victim); // setting up and merging effect data
        effects.addAll(ofEntity(attacker).stream()
                .map(d -> new StatusEffectData(d.event, d.targetInverse(), d.creator, d.effect, d.duration, d.amplifier, d.ambient, d.visible, d.showIcon, d.predicate, !d.shouldReverse, d.ability, d.time)) // inverting
                .toList()
        );

        for (StatusEffectData effect : effects) {
            if (!effect.event.equals(HURT) || effect.shouldReverse) continue;
            LivingEntity toApply = victim;
            if (effect.target.equals("target"))
                toApply = attacker;

            if (predicateManager == null || effect.predicate.isEmpty())
                toApply.addStatusEffect(effect.creator.get());
            else {
                 LootCondition condition = predicateManager.getElement(LootDataType.PREDICATES, effect.predicate.get());
                 if (condition != null) {
                         LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                             .add(LootContextParameters.THIS_ENTITY, toApply) // THIS_ENTITY is whomever the effect is applied to
                             .add(LootContextParameters.ORIGIN, toApply.getPos())
                             .add(LootContextParameters.DAMAGE_SOURCE, event.damageSource)
                             .add(LootContextParameters.KILLER_ENTITY, event.damageSource.getAttacker())
                             .add(LootContextParameters.DIRECT_KILLER_ENTITY, event.damageSource.getSource());
                     if (toApply.getAttacker() instanceof PlayerEntity player)
                        builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, player);
                     if (condition.test(new LootContext.Builder(builder.build(LootContextTypes.ENTITY)).build(null)))
                        toApply.addStatusEffect(effect.creator.get());
                 } else
                    Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
            }
        }
    }

    public static void onAbility(PropertyApplication.ApplicationEvent<PropertyApplication.Ability> event, PropertyApplication.Ability ability) {
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
                         if (condition.test(new LootContext.Builder(builder.build(PropertyApplication.Ability.LOOT_CONTEXT)).build(null)))
                            ability.user().addStatusEffect(effect.creator.get());
                     } else
                        Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
                }
            }
        }
    }

    public static List<StatusEffectData> ofEntity(LivingEntity entity) {
        List<StatusEffectData> list = property.get(entity.getMainHandStack());
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        StatusEffectData.CODEC(new ItemModule.ModuleInstance(ItemModule.empty)).listOf().parse(JsonOps.INSTANCE, data).getOrThrow(false, s -> {
        });
        return true;
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

    public List<StatusEffectData> get(ItemStack itemStack) {
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element == null) {
            return null;
        }
        return StatusEffectData.CODEC(ItemModule.getModules(itemStack)).listOf().parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
        });
    }

    public static List<StatusEffectData> createCache(ItemStack stack) {
        ItemModule.ModuleInstance root = ItemModule.getModules(stack);
        List<StatusEffectData> data = new ArrayList<>();
        for (ItemModule.ModuleInstance module : root.allSubModules()) {
            setupModuleEffects(module, data);
        }
        return data;
    }

    public static void setupModuleEffects(ItemModule.ModuleInstance module, List<StatusEffectData> list) {
        JsonElement element = module.getProperties().get(property);
        if (element == null) return;

        list.addAll(StatusEffectData.CODEC(module).listOf().parse(JsonOps.INSTANCE, element).getOrThrow(false, s -> {
        }));
    }

    public record StatusEffectData(PropertyApplication.ApplicationEvent<?> event, String target,
                                   Supplier<StatusEffectInstance> creator, StatusEffect effect, int duration,
                                   int amplifier, boolean ambient, boolean visible, boolean showIcon,
                                   Optional<Identifier> predicate, boolean shouldReverse,
                                   Optional<ItemUseAbility> ability, Optional<IntegerRange> time) {
        public static Codec<StatusEffectData> CODEC(ItemModule.ModuleInstance instance) {
            return RecordCodecBuilder.create(inst -> inst.group(
                    Codec.STRING.fieldOf("event").forGetter(i -> i.event.name),
                    Codec.STRING.fieldOf("target").forGetter(i -> i.target),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("duration").forGetter(i -> i.duration),
                    StatResolver.Codecs.INTEGER(instance).fieldOf("amplifier").forGetter(i -> i.amplifier),
                    Registries.STATUS_EFFECT.getCodec().fieldOf("effect").forGetter(i -> i.effect), // 1.19.3+ this turns into BuiltinRegistries.STATUS_EFFECT...
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter(i -> i.ambient),
                    Codec.BOOL.optionalFieldOf("visible", true).forGetter(i -> i.visible),
                    Codec.BOOL.optionalFieldOf("showIcon", true).forGetter(i -> i.showIcon),
                    Identifier.CODEC.optionalFieldOf("predicate").forGetter(i -> i.predicate), // allow loot table predicates. ENTITY context is provided (see LootContextTypes.ENTITY)
                    Codec.pair(Codec.STRING.optionalFieldOf("name").codec(), IntegerRange.CODEC.optionalFieldOf("useTime").codec()).optionalFieldOf("ability").forGetter(i -> Optional.of(new Pair<>(i.ability.map(ItemAbilityManager.useAbilityRegistry::findKey), i.time)))
            ).apply(inst, (event, target, dur, amp, eff, am, vis, icon, predicate, ability) -> {
                Optional<String> name = Optional.empty();
                Optional<IntegerRange> time = Optional.empty();
                if (ability.isPresent()) {
                    name = ability.get().getFirst();
                    time = ability.get().getSecond();
                }
                return StatusEffectData.create(event, target, dur, amp, eff, am, vis, icon, predicate, name, time);
            }));
        }

        public static StatusEffectData create(String applyEvent, String applyTarget, int duration, int amplifier, StatusEffect effect, boolean ambient, boolean visible, boolean showIcon, Optional<Identifier> predicateLocation, Optional<String> abilityName, Optional<IntegerRange> time) {
            return new StatusEffectData(
                    PropertyApplication.ApplicationEvent.get(applyEvent),
                    applyTarget,
                    () -> new StatusEffectInstance(effect, duration, amplifier, ambient, visible, showIcon),
                    effect,
                    duration,
                    amplifier,
                    ambient,
                    visible,
                    showIcon,
                    predicateLocation,
                    applyEvent.equalsIgnoreCase("attack"),
                    abilityName.map(ItemAbilityManager.useAbilityRegistry::get),
                    time
            );
        }

        public String targetInverse() {
            switch (target) {
                case "this" -> {
                    return "target";
                }
                case "target" -> {
                    return "this";
                }
            }
            return "this";
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
