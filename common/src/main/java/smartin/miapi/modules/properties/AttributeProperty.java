package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.units.qual.A;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.item.modular.items.ExampleModularItem;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

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
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            JsonObject attributeJson = attributeElement.getAsJsonObject();
            String attributeName = attributeJson.get("attribute").getAsString();
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
                modifierName = attributeJson.get("name").getAsString();
            }

            if (attribute != null) {
                if (uuid != null) {
                    // Use constructor with UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, modifierName, value, operation), slot, true));
                } else {
                    // Use constructor without UUID
                    attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(modifierName, value, operation), slot, true));
                }
            }
        }
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case SMART, EXTEND -> {
                JsonElement element = old.deepCopy();
                element.getAsJsonArray().addAll(toMerge.getAsJsonArray());
                return element;
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

    public static Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiersForSlot(ItemStack itemStack, EquipmentSlot slot, Multimap<EntityAttribute, EntityAttributeModifier> toAdding) {
        if (itemStack.getItem() instanceof ModularItem) {
            Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> toMerge = AttributeProperty.getAttributeModifiers(itemStack);
            Multimap<EntityAttribute, EntityAttributeModifier> merged = ArrayListMultimap.create();
            Multimap<EntityAttribute, EntityAttributeModifier> mergedOnItem = ArrayListMultimap.create();

            // Add existing modifiers from original to merged
            toAdding.entries().forEach(entry -> merged.put(entry.getKey(), entry.getValue()));

            toMerge.forEach((entityAttribute, entityAttributeModifier) -> {
                if (entityAttributeModifier.slot().equals(slot)) {
                    if (entityAttributeModifier.seperateOnItem) {
                        Miapi.LOGGER.error("nonItem");
                        merged.put(entityAttribute, entityAttributeModifier.attributeModifier());
                    } else {
                        mergedOnItem.put(entityAttribute, entityAttributeModifier.attributeModifier());
                    }
                }
            });

            // Assign merged back to original
            toAdding.clear();
            toAdding.putAll(merged);
            Map<UUID, Multimap<EntityAttribute, EntityAttributeModifier>> uuidMultimapMap = new HashMap<>();

            mergedOnItem.forEach((attribute, attributeModifier) -> {
                Multimap<EntityAttribute, EntityAttributeModifier> multimap = uuidMultimapMap.computeIfAbsent(attributeModifier.getId(), (id) -> ArrayListMultimap.create());
                multimap.put(attribute, attributeModifier);
            });

            uuidMultimapMap.forEach((uuid, entityAttributeEntityAttributeModifierMultimap) -> {
                entityAttributeEntityAttributeModifierMultimap.asMap().forEach((key, collection) -> {
                    double startValue = key.getDefaultValue();
                    double multiply = 1;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.ADDITION)) {
                            startValue += entityAttributeModifier.getValue();
                        }
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
                            multiply += entityAttributeModifier.getValue();
                        }
                    }
                    startValue = startValue * multiply;
                    for (EntityAttributeModifier entityAttributeModifier : collection) {
                        if (entityAttributeModifier.getOperation().equals(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
                            startValue = startValue * entityAttributeModifier.getValue();
                        }
                    }
                    startValue = startValue - key.getDefaultValue();
                    EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(uuid, "generic.miapi." + key.getTranslationKey(), startValue, EntityAttributeModifier.Operation.ADDITION);
                    toAdding.put(key, entityAttributeModifier);
                });
            });
            return toAdding;
        }
        return toAdding;
    }

    public static double getActualValue(ItemStack stack, EquipmentSlot slot, EntityAttribute entityAttribute) {
        Collection<EntityAttributeModifier> attributes = stack.getAttributeModifiers(slot).get(entityAttribute);
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        attributes.forEach(attribute -> {
            map.put(entityAttribute, attribute);
        });

        DefaultAttributeContainer container = DefaultAttributeContainer.builder().add(entityAttribute).build();

        AttributeContainer container1 = new AttributeContainer(container);

        container1.addTemporaryModifiers(map);

        return container1.getValue(entityAttribute);
    }

    private static Multimap<EntityAttribute, EntityAttributeModifierHolder> createAttributeCache(ItemStack itemStack) {
        ItemModule.ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (ItemModule.ModuleInstance instance : rootInstance.allSubModules()) {
            getAttributeModifiers(instance, attributeModifiers);
        }
        return attributeModifiers;
    }

    public static void getAttributeModifiers(ItemModule.ModuleInstance instance, Multimap<EntityAttribute, EntityAttributeModifierHolder> attributeModifiers) {
        JsonElement element = instance.getProperties().get(property);
        if (element == null) {
            return;
        }
        for (JsonElement attributeElement : element.getAsJsonArray()) {
            AttributeJson attributeJson = Miapi.gson.fromJson(attributeElement, AttributeJson.class);
            assert attributeJson.attribute != null;
            assert attributeJson.value != null;
            assert attributeJson.operation != null;
            EquipmentSlot slot = (attributeJson.slot != null) ? getSlot(attributeJson.slot) : EquipmentSlot.MAINHAND;
            String attributeName = attributeJson.attribute;
            double value = StatResolver.resolveDouble(attributeJson.value, instance);
            EntityAttributeModifier.Operation operation = getOperation(attributeJson.operation);
            EntityAttribute attribute = Registry.ATTRIBUTE.get(new Identifier(attributeJson.attribute));
            assert attribute != null;
            if (attributeJson.uuid != null) {
                UUID uuid = UUID.fromString(attributeJson.uuid);
                // Thanks Mojang for using == and not .equals so i have to do this abomination
                if (uuid.equals(ExampleModularItem.attackDamageUUID())) {
                    uuid = ExampleModularItem.attackDamageUUID();
                }
                if (uuid.equals(ExampleModularItem.attackSpeedUUID())) {
                    uuid = ExampleModularItem.attackSpeedUUID();
                }
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(uuid, attributeName, value, operation), slot, attributeJson.seperateOnItem));
            } else {
                // Use constructor without UUID
                attributeModifiers.put(attribute, new EntityAttributeModifierHolder(new EntityAttributeModifier(attributeName, value, operation), slot, attributeJson.seperateOnItem));
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

    public record EntityAttributeModifierHolder(EntityAttributeModifier attributeModifier, EquipmentSlot slot,
                                                boolean seperateOnItem) {
    }

    public class AttributeJson {
        public String attribute;
        public String value;
        public String operation;
        public String slot;
        public String uuid;
        public boolean seperateOnItem;
    }
}
