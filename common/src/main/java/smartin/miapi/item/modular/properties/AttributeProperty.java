package smartin.miapi.item.modular.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.cache.ModularItemCache;

import java.util.UUID;

public class AttributeProperty implements ModuleProperty {
    public static final String key = "attributes";
    public static ModuleProperty attributesProperty;

    public AttributeProperty() {
        this.attributesProperty = this;
        ModularItemCache.setSupplier(key, (stack -> createAttributeCache(stack)));
    }

    @Override
    public boolean load(String moduleKey, JsonElement element) throws Exception {
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        JsonObject attributesJson = element.getAsJsonObject();
        for (String attributeName : attributesJson.keySet()) {
            JsonObject attributeJson = attributesJson.getAsJsonObject(attributeName);
            double value = StatResolver.resolveDouble(attributeJson.get("value").getAsString(), new ItemModule.ModuleInstance(ItemModule.empty));
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.get("operation").getAsString());
            EquipmentSlot slot = getSlot(attributeJson.get("slot").getAsString());
            EntityAttribute attribute = Registry.ATTRIBUTE.get(new Identifier(attributeName));
            UUID uuid = null;

            if (attributeJson.has("uuid")) {
                uuid = UUID.fromString(attributeJson.get("uuid").getAsString());
            }

            if (attribute != null) {
                if (uuid != null) {
                    // Use constructor with UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, attributeName, value, operation), slot));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(attributeName, value, operation), slot));
                }
            }
        }
        return true;
    }

    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> getAttributeModifiers(ItemStack itemStack) {
        return (Multimap<EntityAttribute, EntityAttributeModifierHolder>) ModularItemCache.get(itemStack, key);
    }

    private static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        ItemModule.ModuleInstance rootInstance = ModularItem.getModules(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        ItemModule.createFlatList(rootInstance).forEach(instance -> {
            getAttributeModifiers(instance, attributeModifiers);
        });
        return attributeModifiers;
    }

    public static void getAttributeModifiers(ItemModule.ModuleInstance instance, Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers) {
        instance.getProperties().get(attributesProperty);
        JsonElement element = instance.getProperties().get(attributesProperty);
        if (element == null) {
            return;
        }
        JsonObject attributesJson = element.getAsJsonObject();
        for (String attributeName : attributesJson.keySet()) {
            JsonObject attributeJson = attributesJson.getAsJsonObject(attributeName);
            double value = StatResolver.resolveDouble(attributeJson.get("value").getAsString(), instance);
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.get("operation").getAsString());
            EquipmentSlot slot = getSlot(attributeJson.get("slot").getAsString());
            EntityAttribute attribute = Registry.ATTRIBUTE.get(new Identifier(attributeName));

            UUID uuid = null;

            if (attributeJson.has("uuid")) {
                uuid = UUID.fromString(attributeJson.get("uuid").getAsString());
            }

            if (attribute != null) {
                if (uuid != null) {
                    // Use constructor with UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, attributeName, value, operation), slot));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(attributeName, value, operation), slot));
                }
            }
        }
    }

    private static EntityAttributeModifier.Operation getOperation(String operationString) {
        switch (operationString) {
            case "+":
                return EntityAttributeModifier.Operation.ADDITION;
            case "*":
                return EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "**":
                return EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
                return EntityAttributeModifier.Operation.ADDITION;
        }
    }

    private static EquipmentSlot getSlot(String slotString) {
        if (slotString != null && !slotString.isEmpty()) {
            try {
                return EquipmentSlot.byName(slotString);
            } catch (Exception e) {
                Miapi.LOGGER.error("Equipment Slot not found - use correct spelling please [mainhand offhand feet legs chest head]");
                Miapi.LOGGER.error("substituting mainhand Slot Instead");
                e.printStackTrace();
            }
        }
        return EquipmentSlot.MAINHAND; // default to main hand if slot is not specified
    }

    public record EntityAttributeModifierHolder(EntityAttributeModifier attributeModifier, EquipmentSlot slot) {
    }
}
