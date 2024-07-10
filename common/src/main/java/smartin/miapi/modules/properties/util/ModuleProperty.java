package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.*;

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
        return Optional.ofNullable(moduleInstance.getProperty(this));
    }

    default Optional<T> getData(ItemStack itemStack) {
        return Optional.ofNullable(ItemModule.getModules(itemStack).getPropertyItemStack(this));
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

    static <K> Map<String, K> mergeMap(Map<String, K> left, Map<String, K> right, MergeType mergeType) {
        if (MergeType.OVERWRITE.equals(mergeType)) {
            return new LinkedHashMap<>(right);
        }
        Map<String, K> merged = new LinkedHashMap<>(left);
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

    static <K> K decideLeftRight(K right, K left, MergeType mergeType) {
        if(MergeType.EXTEND.equals(mergeType)){
            return right;
        }else{
            return left;
        }
    }
}
