package smartin.miapi.craft.stat;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;

public class StatRequirementMap {
    protected final Map<CraftingStat<?>, Object> raw = new HashMap<>();

    public <T> T get(CraftingStat<T> key) {
        return (T) raw.get(key);
    }

    public <T> T put(CraftingStat<T> key, T value) {
        return (T) raw.put(key, value);
    }

    public void putAll(StatRequirementMap other) {
        raw.putAll(other.raw);
    }

    public <T> StatRequirementMap set(CraftingStat<T> key, T val) {
        raw.put(key, val);
        return this;
    }

    public <E> E getOrDefault(CraftingStat<E> stat) {
        E val = get(stat);
        if (val == null) return stat.getDefault();
        return val;
    }

    /**
     * ONLY USE THIS IF YOU KNOW WHAT YOU'RE DOING
     */
    public Map<CraftingStat<?>, Object> getRaw() {
        return raw;
    }

    public static class Codec implements com.mojang.serialization.Codec<StatRequirementMap> {
        private final ModuleInstance modules;

        public Codec(ModuleInstance modules) {
            this.modules = modules;
        }

        @Override
        public <T> DataResult<Pair<StatRequirementMap, T>> decode(DynamicOps<T> ops, T input) {
            StatRequirementMap stats = new StatRequirementMap();
            MapLike<T> map = ops.getMap(input).getOrThrow(s ->
                    new RuntimeException("Failed to create map in StatRequirementMapCodec! -> " + s));

            map.entries().forEach(p -> {
                String str = ops.getStringValue(p.getFirst()).getOrThrow(s ->
                        new RuntimeException("Failed to getVertexConsumer string value in StatRequirementMapCodec! -> "+s));

                CraftingStat stat = RegistryInventory.craftingStats.get(str);
                T element = p.getSecond();
                if (stat != null) {
                    if (element instanceof JsonElement json) {
                        stats.set(stat, stat.createFromJson(json, modules));
                    } else if (element instanceof Tag nbt) {
                        stats.set(stat, stat.createFromNbt(nbt));
                    } else {
                        stats.set(stat, stat.createFromJson(
                                ExtraCodecs.JSON.parse(ops, element).getOrThrow(s ->
                                        new RuntimeException("Failed to turn instance into a JsonElement while decoding a StatRequirementMap! -> "+ s)),
                                modules
                        ));
                    }
                } // I could warn if the stat id is invalid, but optional compat is a thing and spamming console with invalid id warns is obnoxious af
            });

            return DataResult.success(Pair.of(stats, input));
        }

        @Override // i probably don't even need to define encoding cuz it will likely never be used, but i will anyways
        public <T> DataResult<T> encode(StatRequirementMap input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();

            input.raw.forEach((stat, inst) -> {
                T obj = ExtraCodecs.JSON.encodeStart(ops, ((CraftingStat) stat).saveToJson(inst))
                        .getOrThrow(s -> new RuntimeException("Failed to turn instance into a JsonElement while encoding a StatRequirementMap! -> "+ s));
                map.put(ops.createString(RegistryInventory.craftingStats.findKey(stat)), obj);
            });

            return DataResult.success(ops.createMap(map));
        }
    }
}
