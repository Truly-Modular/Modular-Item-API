package smartin.miapi.item.modular.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.cache.ModularItemCache;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

/**
 * This property allows Modules to set Attributes
 */
public class AttributeProperty implements ModuleProperty {
    public static final String KEY = "attributes";
    public static ModuleProperty property;

    public AttributeProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, (AttributeProperty::createAttributeCache));
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
            String modifierName = attributeName;
            if (attributeJson.has("name")) {
                modifierName =  attributeJson.get("name").getAsString();
            }

            if (attribute != null) {
                if (uuid != null) {
                    // Use constructor with UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, modifierName, value, operation), slot));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(modifierName, value, operation), slot));
                }
            }
        }
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        Type typeToken = new TypeToken<List<JsonElement>>(){}.getType();
        List<JsonElement> oldList  = Miapi.gson.fromJson(old,typeToken);
        List<JsonElement> newList  = Miapi.gson.fromJson(toMerge,typeToken);
        switch (type){
            case SMART,EXTEND -> {
                oldList.addAll(newList);
                return Miapi.gson.toJsonTree(oldList,typeToken);
            }
            case OVERWRITE -> {
                return toMerge;
            }
        }
        return old;
    }

    public static Multimap<EntityAttribute, EntityAttributeModifierHolder> getAttributeModifiers(ItemStack itemStack) {
        return (Multimap<EntityAttribute, EntityAttributeModifierHolder>) ModularItemCache.get(itemStack, KEY);
    }

    private static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        ItemModule.ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        ItemModule.createFlatList(rootInstance).forEach(instance -> {
            getAttributeModifiers(instance, attributeModifiers);
        });
        return attributeModifiers;
    }

    public static void getAttributeModifiers(ItemModule.ModuleInstance instance, Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers) {
        instance.getProperties().get(property);
        JsonElement element = instance.getProperties().get(property);
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
