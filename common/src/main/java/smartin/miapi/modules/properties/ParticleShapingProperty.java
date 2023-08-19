package smartin.miapi.modules.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.datapack.codec.ValueTester;
import net.minecraft.util.Identifier;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;
import java.util.Optional;

//todo finish new morphing system and add to this
public class ParticleShapingProperty extends CodecBasedProperty<List<ParticleShapingProperty.Holder>> {
    public static final String KEY = "particleShaping";
    private static ParticleShapingProperty property;

    public ParticleShapingProperty() {
        super(KEY, null);
        //AppEventOld.getAllEvents().forEach(e -> e.addListener(this));
        property = this;
    }

    /*@Override
    public <E> void onEvent(AppEventOld<E> main, E instance) {
        if (main instanceof AppEventOld.EntityHolding<E> event) {
            LivingEntity entity = event.getEntity(instance);
            if (!(entity.getWorld() instanceof ServerWorld world)) return;
            ItemStack stack = event.stackGetter.apply(instance);
            if (stack == null) stack = entity.getMainHandStack();

            List<Holder> holders = property.get(stack);
            if (holders == null) return;

            for (Holder holder : holders) {
                if (!holder.event.equals(event)) continue;

                if (instance instanceof PropAppOld.Ability ability && holder.abilityConditions.isPresent() && !holder.abilityConditions.get().test(ability)) continue; // used to check ability name and time
                if (holder.predicate.isPresent()) {
                    LootManager manager = world.getServer().getLootManager();
                    if (manager != null) {
                        LootCondition condition = manager.getElement(LootDataType.PREDICATES, holder.predicate.get());
                        if (condition != null) {
                            LootContextParameterSet.builder builder = new LootContextParameterSet.builder(world)
                                    .add(LootContextParameters.THIS_ENTITY, entity)
                                    .add(LootContextParameters.ORIGIN, entity.getPos());
                            if (!condition.test(new LootContext.builder(builder.build(LootContextTypes.SELECTOR)).build(null))) continue;
                        } else
                            Miapi.LOGGER.warn("Found null predicate during ParticleShapingProperty shaping.");
                    }
                }

                Quaterniond quaterniond = new Quaterniond();
                if (holder.align.getFirst() || holder.align.getSecond())
                    quaterniond.rotationYXZ(
                            holder.align.getFirst() ? -Math.toRadians(entity.getHeadYaw() % 360) : 0,
                            holder.align.getSecond() ? Math.toRadians(entity.getPitch() % 360) : 0,
                            0
                    );

                holder.shaper.fromServer().transform(quaterniond).runAt(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
            }
        }
    }*/

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return array;
            }
        }
        return old;
    }

    public static class Holder {
       /* private static final Codec<ValueTester<PropAppOld.Ability>> abConditions = new ValueTester.builder<PropAppOld.Ability>()
                .add("name", Codec.STRING, (ab, name) -> {
                    if (name.equals("$all")) return true;
                    ItemUseAbility ability = ItemAbilityManager.useAbilityRegistry.get(name);
                    if (ability == null) return false;
                    return ability.equals(ab.ability());
                }, "$all")
                .add("time", PotionEffectProperty.IntegerRange.CODEC, (ab, range) -> range.test(ab.useTime()), new PotionEffectProperty.IntegerRange(-1, Optional.empty()))
                .codec();

        private static final Codec<Holder> partialCodec = RecordCodecBuilder.create(inst -> inst.group(
                AppEventOld.CODEC.fieldOf("event").forGetter(i -> i.event),
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
        private final AppEventOld<?> event;
        private final Optional<ValueTester<PropAppOld.Ability>> abilityConditions;
        private final Optional<Identifier> predicate;
        private final Pair<Boolean, Boolean> align;

        private Holder(JsonParticleShaping.StoringParticleShaper shaper, AppEventOld<?> event, Optional<ValueTester<PropAppOld.Ability>> abilityConditions, Optional<Identifier> predicate, Pair<Boolean, Boolean> align) {
            this.shaper = shaper;
            this.event = event;
            this.abilityConditions = abilityConditions;
            this.predicate = predicate;
            this.align = align;
        }*/
    }
}
