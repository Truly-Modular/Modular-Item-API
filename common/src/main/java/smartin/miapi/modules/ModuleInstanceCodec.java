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

import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleInstanceCodec implements Codec<ModuleInstance> {

    private final Codec<Map<ResourceLocation, JsonElement>> dataJsonCodec;
    private final Codec<Map<String, ModuleInstance>> childrenCodec;

    public ModuleInstanceCodec() {
        this.dataJsonCodec = Codec.unboundedMap(
                Miapi.ID_CODEC,
                StatResolver.Codecs.JSONELEMENT_CODEC
        );

        this.childrenCodec = Codec.unboundedMap(
                Codec.STRING,
                this
        );
    }

    @Override
    public <T> DataResult<Pair<ModuleInstance, T>> decode(DynamicOps<T> ops, T input) {
        RegistryOps.RegistryInfoLookup lookup;
        if (ops instanceof RegistryOps<?> registryOps) {
            lookup = ((RegistryOpsAccessor) registryOps).getMiapiLookupProvider();
        }else{
            lookup = new MiapiHolderLookupAdapter(Miapi.registryAccess);
        }
        return ops.getMap(input).flatMap(map -> {

            DataResult<ResourceLocation> keyResult =
                    Miapi.ID_CODEC.parse(ops, map.get("key"));

            DataResult<Map<ResourceLocation, JsonElement>> dataResult;
            T data = map.get("data");

            if (data != null) {
                dataResult = dataJsonCodec.parse(ops, data);
            } else {
                dataResult = DataResult.success(new LinkedHashMap<>());
            }

            DataResult<Map<String, ModuleInstance>> childrenResult;

            T childData = map.get("child");
            if (childData != null) {
                childrenResult = childrenCodec.parse(ops, childData);
            } else {
                childrenResult = DataResult.success(new LinkedHashMap<>());
            }

            return keyResult.flatMap(key ->
                    dataResult.flatMap(dataMap ->
                            childrenResult.map(children -> {

                                ModuleInstance instance = new ModuleInstance(
                                        key,
                                        dataMap,
                                        children,
                                        lookup
                                );
                                return Pair.of(instance, input);
                            })
                    )
            );
        });
    }

    @Override
    public <T> DataResult<T> encode(ModuleInstance input, DynamicOps<T> ops, T prefix) {

        Map<T, T> values = new LinkedHashMap<>();

        // key
        Miapi.ID_CODEC.encodeStart(ops, input.moduleId())
                .resultOrPartial(Miapi.LOGGER::warn)
                .ifPresent(keyElement ->
                        values.put(ops.createString("key"), keyElement)
                );

        // children (String keyed map)
        if (!input.children().isEmpty()) {
            childrenCodec.encodeStart(ops, input.children())
                    .resultOrPartial(Miapi.LOGGER::warn)
                    .ifPresent(childElement ->
                            values.put(ops.createString("child"), childElement)
                    );
        }

        // data
        if (!input.data().isEmpty()) {
            dataJsonCodec.encodeStart(ops, input.data())
                    .resultOrPartial(Miapi.LOGGER::warn)
                    .ifPresent(dataElement ->
                            values.put(ops.createString("data"), dataElement)
                    );
        }

        return DataResult.success(ops.createMap(values));
    }

    // ---- RegistryOps lookup extraction ----

    private static RegistryOps.RegistryInfoLookup getLookup(RegistryOps<?> ops) {
        try {
            var field = ops.getClass().getDeclaredField("lookupProvider");
            field.setAccessible(true);
            return (RegistryOps.RegistryInfoLookup) field.get(ops);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract RegistryInfoLookup from RegistryOps", e);
        }
    }
}