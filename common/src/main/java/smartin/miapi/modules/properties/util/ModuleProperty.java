package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

public interface ModuleProperty<T> {

    T decode(JsonElement element);

    JsonElement encode(T property);

    T merge(T left, T right, MergeType mergeType);

    default T merge(T left, @Nullable ModuleInstance leftModule, T right, @Nullable ModuleInstance rightModule, MergeType mergeType) {
        return merge(left, right, mergeType);
    }

    default boolean load(ResourceLocation id, JsonElement element, boolean isClient) {
        decode(element);
        return true;
    }

    default T getProperty(ModuleInstance moduleInstance) {
        return moduleInstance.getProperty(this);
    }

    default T getProperty(ItemStack itemStack) {
        return ItemModule.getModules(itemStack).getPropertyItemStack(this);
    }

    default T initialize(T property, ModuleInstance context) {
        return property;
    }
}
