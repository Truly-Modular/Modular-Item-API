package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * A more dynamic form of {@link CodecBasedProperty}. This should be used when you want to have collections, maps, etc. of data,
 * or want to resolve material data with the {@link StatResolver}.
 *
 * @param <T> The type of root object that will be added into the instance of {@code A}.
 * @param <A> The type of holder object (eg. List) that will holld your {@code T}'s.
 */
public abstract class DynamicCodecBasedProperty<T, A> implements ModuleProperty {
    private final String key;

    public DynamicCodecBasedProperty(String key) {
        this.key = key;
        ModularItemCache.setSupplier(key, this::createCache);
    }

    public A createCache(ItemStack stack) {
        A holder = createNewHolder();
        ItemModule.ModuleInstance rootInstance = ItemModule.getModules(stack);
        for (ItemModule.ModuleInstance subModule : rootInstance.allSubModules()) {
            JsonElement element = subModule.getProperties().get(this);
            if (element == null) continue;
            T deserialized = getDataFromSubModule(element, subModule);
            addTo(subModule, deserialized, holder);
        }

        return holder;
    }

    public abstract void addTo(ItemModule.ModuleInstance module, T object, A holder);
    public abstract A createNewHolder();
    public abstract Codec<T> codec(ItemModule.ModuleInstance instance);
    public T getDataFromSubModule(JsonElement element, ItemModule.ModuleInstance module) {
        return codec(module).parse(JsonOps.INSTANCE, element).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to deserialize module data for DynamicCodecBasedProperty! -> {}", s));
    }

    public A get(ItemStack itemStack) {
        return (A) ModularItemCache.get(itemStack, key);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        codec(new ItemModule.ModuleInstance(ItemModule.empty)).parse(JsonOps.INSTANCE, data).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to load DynamicCodecBasedProperty! -> {}", s));
        return true;
    }

    public static abstract class CollectedList<T> extends DynamicCodecBasedProperty<T, List<T>> {
        public CollectedList(String key) {
            super(key);
        }

        @Override
        public void addTo(ItemModule.ModuleInstance module, T object, List<T> holder) {
            holder.add(object);
        }

        @Override
        public List<T> createNewHolder() {
            return new ArrayList<>();
        }
    }
    public static abstract class FlattenedList<T> extends DynamicCodecBasedProperty<List<T>, List<T>> {
        public FlattenedList(String key) {
            super(key);
        }

        @Override
        public void addTo(ItemModule.ModuleInstance module, List<T> object, List<T> holder) {
            holder.addAll(object);
        }

        @Override
        public List<T> createNewHolder() {
            return new ArrayList<>();
        }

        @Override
        public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
            return ModuleProperty.mergeList(old, toMerge, type);
        }
    }
    public static class IntermediateList<I, O> extends FlattenedList<O> {
        protected final Codec<List<I>> codec;
        protected final BiFunction<I, ItemModule.ModuleInstance, O> converter;

        public IntermediateList(String key, Codec<List<I>> codec, BiFunction<I, ItemModule.ModuleInstance, O> converter) {
            super(key);
            this.codec = codec;
            this.converter = converter;
        }

        @Override
        public Codec<List<O>> codec(ItemModule.ModuleInstance instance) {
            return null;
        }

        @Override
        public List<O> getDataFromSubModule(JsonElement element, ItemModule.ModuleInstance module) {
            List<I> input = codec.parse(JsonOps.INSTANCE, element).getOrThrow(false, s ->
                    Miapi.LOGGER.error("Failed to deserialize module data for DynamicCodecBasedProperty.IntermediateList! -> {}", s));
            return input.stream().map(i -> converter.apply(i, module)).toList();
        }

        @Override
        public boolean load(String moduleKey, JsonElement data) throws Exception {
            codec.parse(JsonOps.INSTANCE, data).getOrThrow(false, s ->
                    Miapi.LOGGER.error("Failed to load data for DynamicCodecBasedProperty.IntermediateList! -> {}", s));
            return true;
        }
    }
}
