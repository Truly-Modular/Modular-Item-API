package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

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

    default T getData(ModuleInstance moduleInstance) {
        return moduleInstance.getProperty(this);
    }

    default T getData(ItemStack itemStack) {
        return ItemModule.getModules(itemStack).getPropertyItemStack(this);
    }

    default T initialize(T property, ModuleInstance context) {
        return property;
    }
}
