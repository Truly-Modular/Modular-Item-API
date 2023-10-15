package smartin.miapi.events.property;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ActionListeningProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public class PropertyActionManager {
    public static final Map<Entity, Map<String, Supplier<ItemStack>>> activeSlots = new WeakHashMap<>();
    public static final String cacheKey = "propertyActionListeners";

    public void setup() {
        ModularItemCache.setSupplier(cacheKey, stack -> {
            ActionListeningProperty.EventMap map = new ActionListeningProperty.EventMap();
            ItemModule.getModules(stack).getProperties().keySet().stream()
                    .filter(ActionListeningProperty.class::isInstance)
                    .forEach(p -> {
                        ActionListeningProperty property = (ActionListeningProperty) p;
                        map.putAll(property.getAllListeners(stack));
                    });
            return map;
        });
    }

    public static void setActiveSlot(Entity entity, String slot, Supplier<ItemStack> stack) {
        getOrCreateSlots(entity).put(slot, stack);
    }

    public static Map<String, Supplier<ItemStack>> getOrCreateSlots(Entity entity) {
        return activeSlots.computeIfAbsent(entity, k -> new HashMap<>());
    }

    public static ActionListeningProperty.EventMap getListeners(ItemStack stack) {
        return (ActionListeningProperty.EventMap) ModularItemCache.getRaw(stack, cacheKey);
    }
}
