package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.datapack.codec.ValueTester;
import com.redpxnda.nucleus.datapack.json.JsonParticleShaping;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.CodecBasedEventProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.PropertyApplication;

import java.util.Optional;

public class ParticleShapingProperty extends CodecBasedEventProperty<ParticleShapingProperty.Holder> {
    public static final String KEY = "particleShaping";
    private static ParticleShapingProperty property;

    public ParticleShapingProperty() {
        super(
                KEY,
                true,
                new EventHandlingMap<>()
                        .setAll(PropertyApplication.ApplicationEvent.ABILITIES, ParticleShapingProperty::onAbility),
                holder -> holder.event
        );

        property = this;
    }

    protected static void onAbility(PropertyApplication.ApplicationEvent<PropertyApplication.Ability> event, PropertyApplication.Ability ability) {
        if (!(ability.world() instanceof ServerWorld world)) return;
        Holder holder = property.get(ability.stack());

        if (holder.abilityConditions.isPresent() && !holder.abilityConditions.get().test(ability)) return; // used to check ability name and time
        if (holder.predicate.isPresent()) {
            LootManager manager = world.getServer().getLootManager();
            if (manager != null) {
                LootCondition condition = manager.getElement(LootDataType.PREDICATES, holder.predicate.get());
                if (condition != null) {
                    LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                            .add(LootContextParameters.THIS_ENTITY, ability.user())
                            .add(LootContextParameters.ORIGIN, ability.user().getPos());
                    if (!condition.test(new LootContext.Builder(builder.build(LootContextTypes.SELECTOR)).build(null))) return;
                } else
                    Miapi.LOGGER.warn("Found null predicate during ParticleShapingProperty shaping.");
            }
        }

        Quaterniond quaterniond = new Quaterniond();
        if (holder.align.getFirst() || holder.align.getSecond())
            quaterniond.rotationYXZ(
                    holder.align.getFirst() ? -Math.toRadians(ability.user().getHeadYaw() % 360) : 0,
                    holder.align.getSecond() ? Math.toRadians(ability.user().getPitch() % 360) : 0,
                    0
            );

        holder.shaper.fromServer().transform(quaterniond).runAt(ability.world(), ability.user().getX(), ability.user().getY(), ability.user().getZ());
    }

    @Override
    public Codec<ParticleShapingProperty.Holder> codec(ItemModule.ModuleInstance instance) {
        return Holder.codec;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        return super.merge(old, toMerge, type);
    }

    public static class Holder {
        private static final Codec<ValueTester<PropertyApplication.Ability>> abConditions = new ValueTester.Builder<PropertyApplication.Ability>()
                .add("name", Codec.STRING, (ab, name) -> {
                    if (name.equals("$all")) return true;
                    ItemUseAbility ability = ItemAbilityManager.useAbilityRegistry.get(name);
                    if (ability == null) return false;
                    return ability.equals(ab.ability());
                }, "$all")
                .add("time", PotionEffectProperty.IntegerRange.CODEC, (ab, range) -> range.test(ab.useTime()), new PotionEffectProperty.IntegerRange(-1, Optional.empty()))
                .codec();

        private static final Codec<Holder> partialCodec = RecordCodecBuilder.create(inst -> inst.group(
                PropertyApplication.ApplicationEvent.CODEC.fieldOf("event").forGetter(i -> i.event),
                abConditions.optionalFieldOf("ability").forGetter(i -> i.abilityConditions),
                Identifier.CODEC.optionalFieldOf("predicate").forGetter(i -> i.predicate),
                Codec.pair(
                        Codec.BOOL.fieldOf("yaw").codec(),
                        Codec.BOOL.fieldOf("pitch").codec()
                ).optionalFieldOf("align", Pair.of(false, false)).forGetter(i -> i.align)
        ).apply(inst, (e, abCond, p, al) -> new Holder(null, e, abCond, p, al)));
        private static final Codec<Holder> codec = Codec.pair(
                JsonParticleShaping.completeCodec,
                partialCodec
        ).xmap(
                p -> new Holder(
                        p.getFirst(), p.getSecond().event, p.getSecond().abilityConditions,
                        p.getSecond().predicate, p.getSecond().align
                ), h -> Pair.of(h.shaper, new Holder(
                        null, h.event, h.abilityConditions,
                        h.predicate, h.align)));

        private final JsonParticleShaping.StoringParticleShaper shaper;
        private final PropertyApplication.ApplicationEvent<?> event;
        private final Optional<ValueTester<PropertyApplication.Ability>> abilityConditions;
        private final Optional<Identifier> predicate;
        private final Pair<Boolean, Boolean> align;

        private Holder(JsonParticleShaping.StoringParticleShaper shaper, PropertyApplication.ApplicationEvent<?> event, Optional<ValueTester<PropertyApplication.Ability>> abilityConditions, Optional<Identifier> predicate, Pair<Boolean, Boolean> align) {
            this.shaper = shaper;
            this.event = event;
            this.abilityConditions = abilityConditions;
            this.predicate = predicate;
            this.align = align;
        }
    }
}
