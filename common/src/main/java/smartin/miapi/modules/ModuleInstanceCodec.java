package smartin.miapi.modules;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.mixin.RegistryOpsAccessor;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ModuleInstanceCodec implements Codec<ModuleInstance> {
    private final Codec<Map<ResourceLocation, JsonElement>> dataJsonCodec;
    private final Codec<Map<String, ModuleInstance>> subModulesCodec;

    public ModuleInstanceCodec() {
        // These should match what's used in the original recursive codec
        this.dataJsonCodec = Codec.unboundedMap(Miapi.ID_CODEC, StatResolver.Codecs.JSONELEMENT_CODEC);
        this.subModulesCodec = Codec.unboundedMap(Codec.STRING, this);
    }

    @Override
    public <T> DataResult<Pair<ModuleInstance, T>> decode(DynamicOps<T> ops, T input) {

        return ops.getMap(input).flatMap(map -> {

            DataResult<ResourceLocation> keyResult = Miapi.ID_CODEC.parse(ops, map.get("key"));

            DataResult<Map<String, ModuleInstance>> childResult;
            T childData = map.get("child");
            if (childData != null) {
                childResult = subModulesCodec.parse(ops, childData);
            } else {
                childResult = DataResult.success(new HashMap<>());
            }

            DataResult<Map<ResourceLocation, JsonElement>> dataResult;
            T data = map.get("data");
            if (data != null) {
                dataResult = dataJsonCodec.parse(ops, data);
            } else {
                dataResult = DataResult.success(new HashMap<>());
            }
            return keyResult.flatMap(key ->
                    childResult.flatMap(children ->
                            dataResult.map(subData -> {
                                ModuleInstance inst = new ModuleInstance(key, children, subData);
                                return Pair.of(inst, input);
                            })
                    )
            );
        });
    }


    @Override
    public <T> DataResult<T> encode(ModuleInstance input, DynamicOps<T> ops, T prefix) {
        Map<T, T> values = new LinkedHashMap<>();

        // Encode "key"
        Miapi.ID_CODEC.encodeStart(ops, input.moduleID).resultOrPartial(Miapi.LOGGER::warn).
                ifPresent(keyElement -> values.put(ops.createString("key"), keyElement));

        // Encode "child"
        if (!input.subModules.isEmpty()) {
            subModulesCodec.encodeStart(ops, input.getSubModuleMapForSave())
                    .resultOrPartial(Miapi.LOGGER::warn)
                    .ifPresent(childElement -> values.put(ops.createString("child"), childElement));
        }

        // Encode "data"
        if (!input.moduleData.isEmpty()) {
            dataJsonCodec.encodeStart(ops, input.getSaveData())
                    .resultOrPartial(Miapi.LOGGER::warn)
                    .ifPresent(dataElement -> values.put(ops.createString("data"), dataElement));
        }


        return DataResult.success(ops.createMap(values));
    }

    public static Codec<ModuleInstance> createWrappedCodec() {
        Codec<ModuleInstance> base = new ModuleInstanceCodec();
        return registrySavingCodec(base, (m, l) -> {
            setupModule(m, l);
            ComponentApplyProperty.trySetup(m);
        });
    }

    private static void setupModule(ModuleInstance moduleInstance, RegistryOps.RegistryInfoLookup lookup) {
        moduleInstance.lookup = lookup;
        moduleInstance.mutable = false;
        moduleInstance.getSubModuleMapForSave().values().forEach(m -> {
            setupModule(m, lookup);
        });
    }

    public static <T> Codec<T> registrySavingCodec(Codec<T> baseCodec, BiConsumer<T, RegistryOps.RegistryInfoLookup> applyLookup) {
        return new Codec<T>() {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
                var basicResult = baseCodec.decode(ops, input);
                if (ops instanceof RegistryOps<T1> registryOps) {
                    if (basicResult.isSuccess()) {
                        applyLookup.accept(basicResult.getOrThrow().getFirst(), ((RegistryOpsAccessor) registryOps).getLookupProvider());
                    }
                }
                return basicResult;
            }

            @Override
            public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
                return baseCodec.encode(input, ops, prefix);
            }
        };
    }
}
