package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
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
        ModuleInstance rootInstance = ItemModule.getModules(stack);
        for (ModuleInstance subModule : rootInstance.allSubModules()) {
            JsonElement element = subModule.getProperties().get(this);
            if (element == null) continue;
            T deserialized = getDataFromSubModule(element, subModule);
            addTo(subModule, deserialized, holder);
        }

        return holder;
    }

    public abstract void addTo(ModuleInstance module, T object, A holder);
    public abstract A createNewHolder();
    public abstract Codec<T> codec(ModuleInstance instance);
    public T getDataFromSubModule(JsonElement element, ModuleInstance module) {
        return codec(module).parse(JsonOps.INSTANCE, element).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to deserialize module data for DynamicCodecBasedProperty! -> {}", s));
    }

    @Nullable
    public A get(ItemStack itemStack) {
        return ModularItemCache.getRaw(itemStack, key);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        codec(new ModuleInstance(ItemModule.empty)).parse(JsonOps.INSTANCE, data).getOrThrow(false, s ->
                Miapi.LOGGER.error("Failed to load DynamicCodecBasedProperty! -> {}", s));
        return true;
    }

    /**
     * Collected lists take individual objects from modules with your property and combine it into a single list.
     *
     * @param <T> The type of object inside the lists.
     */
    public static abstract class CollectedList<T> extends DynamicCodecBasedProperty<T, List<T>> {
        public CollectedList(String key) {
            super(key);
        }

        @Override
        public void addTo(ModuleInstance module, T object, List<T> holder) {
            holder.add(object);
        }

        @Override
        public List<T> createNewHolder() {
            return new ArrayList<>();
        }
    }

    /**
     * Flattened lists merely collect your property, which is a list, from all modules and flatten it into a single list.
     *
     * @param <T> The type of object inside the lists.
     */
    public static abstract class FlattenedList<T> extends DynamicCodecBasedProperty<List<T>, List<T>> {
        public FlattenedList(String key) {
            super(key);
        }

        @Override
        public void addTo(ModuleInstance module, List<T> object, List<T> holder) {
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

    /**
     * Intermediate lists initially decode from the first type parameter. This is your json format representation.
     * However, what if you want to do additional resolving, like for complex numbers({@link StatResolver})?
     * This is where the second type parameter comes in. With a method defined in your constructor, this will
     * convert instances of the first type parameter into the second, passing in a module instance so the data
     * is correctly resolved and cached.
     *
     * @param <I> The input type, usually your raw data that has yet to be resolved
     * @param <O> The output type, usually the refined data that was created with the inputted module instance
     */
    public static class IntermediateList<I, O> extends FlattenedList<O> {
        protected final Codec<List<I>> codec;
        protected final BiFunction<I, ModuleInstance, O> converter;

        public IntermediateList(String key, Codec<List<I>> codec, BiFunction<I, ModuleInstance, O> converter) {
            super(key);
            this.codec = codec;
            this.converter = converter;
        }

        @Override
        public Codec<List<O>> codec(ModuleInstance instance) {
            return null;
        }

        @Override
        public List<O> getDataFromSubModule(JsonElement element, ModuleInstance module) {
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
