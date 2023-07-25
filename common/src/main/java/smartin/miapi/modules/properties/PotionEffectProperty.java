package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.mixin.LootContextTypesAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

public class PotionEffectProperty extends DynamicCodecBasedProperty.IntermediateList<PotionEffectProperty.Raw, PotionEffectProperty.Holder> {
    public static LootContextType LOOT_CONTEXT =
            LootContextTypesAccessor.register("miapi:loot_context", builder -> builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.TOOL));
    public static final String KEY = "applyPotionEffects";
    public static PotionEffectProperty property;

    public PotionEffectProperty() {
        super(KEY, AutoCodec.of(Raw.class).codec().listOf(), Raw::refine);

        property = this;

        ApplicationEvents.ENTITY_RELATED.startListening(
                (event, entity, stack, data, originals) -> onEntityEvent(event, stack, entity, (Holder) data, originals),
                ApplicationEvents.StackGetterHolder.ofMulti(
                        property::get,
                        list -> list.stream().map(data -> Pair.of(data.item, data)).toList()
                )
        );
    }

    public void onEntityEvent(ApplicationEvent<?, ?, ?> event, ItemStack stack, Entity entity, Holder effect, Object... originals) {
        if (!(entity.getWorld() instanceof ServerWorld world) || !effect.event.equals(event)) return;
        LootManager predicateManager = entity.getServer() == null ? null : entity.getServer().getLootManager();

        Entity target = ApplicationEvents.getEntityForTarget(effect.applyTo, entity, event, originals);
        if (!(target instanceof LivingEntity living)) return;

        if (predicateManager == null || effect.predicate == null)
            living.addStatusEffect(effect.createEffectInstance());
        else {
            LootCondition condition = predicateManager.getElement(LootDataType.PREDICATES, effect.predicate);
            if (condition != null) {
                LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                        .add(LootContextParameters.THIS_ENTITY, living) // THIS_ENTITY is whomever the effect is applied to
                        .add(LootContextParameters.ORIGIN, living.getPos())
                        .add(LootContextParameters.TOOL, stack);
                if (condition.test(new LootContext.Builder(builder.build(LOOT_CONTEXT)).build(null)))
                    living.addStatusEffect(effect.createEffectInstance());
            } else
                Miapi.LOGGER.warn("Found null predicate during PotionEffectProperty application.");
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

    public static class Raw {
        public ApplicationEvent<?, ?, ?> event;
        public String item;
        public @AutoCodec.Optional String applyTo = "this";
        public StatResolver.IntegerFromStat duration;
        public StatResolver.IntegerFromStat amplifier;
        public StatusEffect effect;
        public @AutoCodec.Optional boolean ambient = false;
        public @AutoCodec.Optional boolean visible = true;
        public @AutoCodec.Optional boolean showIcon = true;
        public @Nullable @AutoCodec.Optional Identifier predicate = null;

        public Holder refine(ItemModule.ModuleInstance modules) {
            Holder h = new Holder();
            h.event = event;
            h.item = item;
            h.applyTo = applyTo;
            h.effect = effect;
            h.ambient = ambient;
            h.visible = visible;
            h.showIcon = showIcon;
            h.predicate = predicate;
            h.actualDuration = duration.evaluate(modules);
            h.actualAmplifier = amplifier.evaluate(modules);
            return h;
        }
    }
    public static class Holder extends Raw {
        public int actualDuration;
        public int actualAmplifier;

        public StatusEffectInstance createEffectInstance() {
            return new StatusEffectInstance(effect, actualDuration, actualAmplifier, ambient, visible, showIcon);
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
