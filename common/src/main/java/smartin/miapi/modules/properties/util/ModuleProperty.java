package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

import java.util.Map;
import java.util.Optional;

/**
 * @header Properties
 * @description_start Properties are the main way any stat is assigned to anything
 * They are usually found in an id -> data map like
 * ``` json
 * {
 * "miapi:fire_proof":true
 * }
 * ```
 * Note that in this example "miapi" can be removed as it is implied if no mod id is set
 * Most Properties have more complex inner data than a boolean
 * @desciption_end
 * @path /data_types/properties
 * @keywords Properties, module Properties
 */
public interface ModuleProperty<T> extends MergeAble<T>, InitializeAble<T> {

    T decode(JsonElement element);

    /**
     * this should NOT be called, as most properties do not properly implement this because im lazy.
     */
    JsonElement encode(T property);

    @Override
    default T initialize(ModuleInstance context, T data) {
        return data;
    }

    default T merge(T left, ModuleInstance leftModule, T right, ModuleInstance rightModule, MergeType mergeType) {
        return merge(left, right, mergeType);
    }

    default boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        decode(element);
        return true;
    }

    /**
     * warning! depending on your usage of merge and initialize, this return value should not be modified directly, but copied before that
     *
     * @param moduleInstance
     * @return
     */
    default Optional<T> getData(ModuleInstance moduleInstance) {
        if (ReloadEvents.isInReload()) {
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
        if (!ModularItem.isModularItem(itemStack)) {
            return Optional.empty();
        }
        if (ReloadEvents.isInReload()) {
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

    @SuppressWarnings("unchecked")
    default Optional<T> getData(Map<ModuleProperty<?>, Object> properties) {
        return Optional.ofNullable((T) properties.get(this));
    }

    default T initialize(T property, ModuleInstance context) {
        return property;
    }

    static JsonObject mergedJsonObjects(JsonObject left, JsonObject right, MergeType mergeType) {
        if (mergeType.equals(MergeType.OVERWRITE)) {
            return right;
        }
        return deepMergeJsonObjects(left, right);
    }

    static JsonObject deepMergeJsonObjects(JsonObject jsonObject1, JsonObject jsonObject2) {
        // Iterate over the second JsonObject's keys
        for (String key : jsonObject2.keySet()) {
            JsonElement value2 = jsonObject2.get(key);

            // If the key exists in the first object
            if (jsonObject1.has(key)) {
                JsonElement value1 = jsonObject1.get(key);

                // Check if both are JsonObjects, perform a recursive merge
                if (value1.isJsonObject() && value2.isJsonObject()) {
                    JsonObject mergedSubObject = deepMergeJsonObjects(value1.getAsJsonObject(), value2.getAsJsonObject());
                    jsonObject1.add(key, mergedSubObject);
                } else {
                    // If not both are JsonObjects, overwrite with value from jsonObject2
                    jsonObject1.add(key, value2);
                }
            } else {
                // If the key doesn't exist in jsonObject1, add it directly
                jsonObject1.add(key, value2);
            }
        }

        return jsonObject1;
    }
}
