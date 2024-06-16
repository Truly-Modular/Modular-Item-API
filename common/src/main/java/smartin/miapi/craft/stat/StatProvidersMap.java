package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import com.redpxnda.nucleus.util.PriorityMultiMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatProvidersMap {
    protected Map<CraftingStat<?>, Map<StatActorType, PriorityMultiMap<?>>> raw = new HashMap<>();
    public static final Codec MODULELESS_CODEC = new Codec();

    public <T> T evaluate(CraftingStat<T> stat) {
        return StatActorType.evaluate(stat, get(stat));
    }

    public <T> Map<CraftingStat<T>, T> evaluateAll() {
        Map<CraftingStat<T>, T> result = new HashMap<>();

        raw.forEach((stat, map) -> {
            result.put((CraftingStat) stat, (T) StatActorType.evaluate((CraftingStat) stat, (Map) map));
        });

        return result;
    }

    public <T> Map<StatActorType, PriorityMultiMap<T>> get(CraftingStat<T> stat) {
        return (Map) raw.get(stat);
    }

    public <T> void put(CraftingStat<T> stat, Map<StatActorType, PriorityMultiMap<T>> map) {
        raw.put(stat, (Map) map);
    }

    public <T> StatProvidersMap set(CraftingStat<T> stat, StatActorType type, T instance) {
        put(stat, type, instance);
        return this;
    }

    public <T> StatProvidersMap set(CraftingStat<T> stat, StatActorType type, T instance, float prio) {
        put(stat, type, instance, prio);
        return this;
    }

    public <T> void put(CraftingStat<T> stat, StatActorType type, T instance, float prio) {
        Map<StatActorType, PriorityMultiMap<T>> map = get(stat);
        if (map == null) {
            map = new HashMap<>();
            put(stat, map);
        }

        PriorityMultiMap<T> instances = map.get(type);
        if (instances == null) {
            instances = new PriorityMultiMap<>();
            map.put(type, instances);
        }

        instances.put(instance, prio);
    }

    public void putAll(StatProvidersMap map) {
        map.raw.forEach((stat, inner) -> {
            inner.forEach((actor, prioMap) -> {
                prioMap.forEach((inst, prio) -> put((CraftingStat) stat, actor, inst, prio));
            });
        });
    }

    <T> void put(CraftingStat<T> stat, StatActorType type, T instance) {
        put(stat, type, instance, 0f);
    }

    /**
     * ONLY USE THIS IF YOU KNOW WHAT YOU'RE DOING.
     */
    public Map<CraftingStat<?>, Map<StatActorType, PriorityMultiMap<?>>> getRaw() {
        return raw;
    }

    public static class Codec implements com.mojang.serialization.Codec<StatProvidersMap> {
        private final @Nullable ModuleInstance modules;

        public Codec() {
            this.modules = null;
        }

        public Codec(@Nullable ModuleInstance modules) {
            this.modules = modules;
        }

        @Override
        public <T> DataResult<Pair<StatProvidersMap, T>> decode(DynamicOps<T> ops, T input) {
            StatProvidersMap result = new StatProvidersMap();
            MapLike<T> map = ops.getMap(input).getOrThrow(false, s ->
                    Miapi.LOGGER.error("Failed to create base map in StatProvidersMapCodec! -> {}", s));

            map.entries().forEach(p -> {
                String str = ops.getStringValue(p.getFirst()).getOrThrow(false, s ->
                        Miapi.LOGGER.error("Failed to create string value in StatRequirementMapCodec! -> {}", s));

                CraftingStat stat = RegistryInventory.craftingStats.get(str);
                if (stat == null) return; // i could warn if the stat doesn't exist, but optional compat exists

                ops.getStream(p.getSecond()).getOrThrow(false, s -> Miapi.LOGGER.error("Failed to getVertexConsumer data as a list for stat '{}' in StatProvidersMapCodec! -> {}", str, s)).forEach(val -> {
                    MapLike<T> details = ops.getMap(val).getOrThrow(false, s ->
                            Miapi.LOGGER.error("Failed to parse details for stat '{}' in StatProvidersMapCodec! -> {}", str, s));

                    T actorTypeRaw = details.get("type");
                    String actorType = actorTypeRaw == null ? "anchor" : ops.getStringValue(actorTypeRaw).getOrThrow(false, s ->
                            Miapi.LOGGER.error("Failed to parse actor type '{}' in StatProvidersMapCodec! -> {}", actorTypeRaw, s));
                    StatActorType actor = StatActorType.REGISTERED.get(actorType);
                    if (actor != null) {
                        T prioRaw = details.get("priority");
                        float prio = prioRaw == null ? 0 : ops.getNumberValue(prioRaw).getOrThrow(false, s ->
                                Miapi.LOGGER.warn("Failed to parse priority for actor in StatProvidersMapCodec! -> {}", s)).floatValue();

                        T valueRaw = details.get("value");

                        if (valueRaw instanceof JsonElement element) {
                            if (modules == null) {
                                Miapi.LOGGER.error("Cannot decode JSON for a stat provider actor without modules context!");
                                throw new RuntimeException("Cannot decode JSON for a stat provider actor without modules context!");
                            }
                            result.put(stat, actor, stat.createFromJson(element, modules), prio);
                        } else if (valueRaw instanceof NbtElement element) {
                            result.put(stat, actor, stat.createFromNbt(element));
                        } else {
                            if (modules == null) {
                                Miapi.LOGGER.error("Cannot decode non-JSON/non-NBT for a stat provider actor without modules context!");
                                throw new RuntimeException("Cannot decode non-JSON/non-NBT for a stat provider actor without modules context!");
                            }
                            result.put(stat, actor, stat.createFromJson(
                                    Codecs.JSON_ELEMENT.parse(ops, valueRaw).getOrThrow(false, s ->
                                            Miapi.LOGGER.error("Failed to turn value into a JsonElement while decoding a StatProviderMap! -> {}", s)),
                                    modules), prio);
                        }
                    } //else Miapi.LOGGER.warn("Unknown actor type key was found: '{}' ... Data: '{}'", actorType, input);
                });
            });

            return DataResult.success(Pair.of(result, input));
        }

        @Override
        public <T> DataResult<T> encode(StatProvidersMap input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();

            input.raw.forEach((stat, actors) -> {
                List<T> objects = new ArrayList<>();

                actors.forEach((actor, prioMap) -> {
                    prioMap.forEach((instance, prio) -> {
                        Map<T, T> details = new HashMap<>();

                        details.put(ops.createString("type"), ops.createString(StatActorType.REGISTERED.inverse().get(actor)));
                        details.put(ops.createString("priority"), ops.createFloat(prio));

                        if (ops instanceof JsonOps) {
                            details.put(ops.createString("value"), (T) ((CraftingStat) stat).saveToJson(instance));
                        } else if (ops instanceof NbtOps)
                            details.put(ops.createString("value"), (T) ((CraftingStat)stat).saveToNbt(instance));
                        else {
                            details.put(
                                    ops.createString("value"),
                                    Codecs.JSON_ELEMENT.encodeStart(ops, ((CraftingStat) stat).saveToJson(instance))
                                            .getOrThrow(false, s -> Miapi.LOGGER.error("Failed to turn instance into a JsonElement while encoding a StatRequirementMap! -> {}", s))
                            );
                        }

                        objects.add(ops.createMap(details));
                    });
                });

                map.put(ops.createString(RegistryInventory.craftingStats.findKey(stat)), ops.createList(objects.stream()));
            });

            return DataResult.success(ops.createMap(map));
        }
    }
}
