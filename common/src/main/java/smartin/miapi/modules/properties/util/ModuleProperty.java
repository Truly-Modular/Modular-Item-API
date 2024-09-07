package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Triple;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.*;
import java.util.function.Function;

public interface ModuleProperty<T> {

    T decode(JsonElement element);

    /**
     * this should NOT be called, as most properties do not properly implement this because im lazy.
     */
    JsonElement encode(T property);

    T merge(T left, T right, MergeType mergeType);

    default T merge(T left, ModuleInstance leftModule, T right, ModuleInstance rightModule, MergeType mergeType) {
        return merge(left, right, mergeType);
    }

    default boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        decode(element);
        return true;
    }

    default Optional<T> getData(ModuleInstance moduleInstance) {
        if(ReloadEvents.isInReload()){
            return Optional.empty();
        }
        if (moduleInstance == null || moduleInstance.module == ItemModule.empty) {
            return Optional.empty();
        }
        return Optional.ofNullable(moduleInstance.getProperty(this));
    }

    default Optional<T> getData(ItemStack itemStack) {
        if (itemStack == null) {
            return Optional.empty();
        }
        if(ReloadEvents.isInReload()){
            return Optional.empty();
        }
        ModuleInstance baseModule = ItemModule.getModules(itemStack);
        if (baseModule == null || baseModule.module == ItemModule.empty) {
            return Optional.empty();
        }
        return Optional.ofNullable(baseModule.getPropertyItemStack(this));
    }

    @SuppressWarnings("unchecked")
    default Optional<T> getData(ItemModule module) {
        return Optional.ofNullable((T) module.properties().get(this));
    }

    default T initialize(T property, ModuleInstance context) {
        return property;
    }

    static <K> List<K> mergeList(List<K> left, List<K> right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return new ArrayList<>(right);
        }
        List<K> merged = new ArrayList<>(left);
        merged.addAll(right);
        return merged;
    }

    static <K, L> Map<L, K> mergeMap(Map<L, K> left, Map<L, K> right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return new LinkedHashMap<>(right);
        }
        Map<L, K> merged = new LinkedHashMap<>(left);
        if (MergeType.EXTEND.equals(mergeType)) {
            right.forEach((key, entry) -> {
                if (!merged.containsKey(key)) {
                    merged.put(key, entry);
                }
            });
            return merged;
        }
        merged.putAll(right);
        return merged;
    }

    static <K, L> Map<L, K> mergeMap(Map<L, K> left, Map<L, K> right, MergeType mergeType, Function<Triple<K, K, MergeType>, K> collisionHandle) {
        Map<L, K> merged = new LinkedHashMap<>(left);
        right.forEach((key, entry) -> {
            if (!merged.containsKey(key)) {
                merged.put(key, entry);
            } else {
                merged.put(key, collisionHandle.apply(Triple.of(merged.get(key), entry, mergeType)));
            }
        });
        return merged;
    }

    static <K> K decideLeftRight(K right, K left, MergeType mergeType) {
        if (MergeType.EXTEND.equals(mergeType)) {
            return right;
        } else {
            return left;
        }
    }
}
