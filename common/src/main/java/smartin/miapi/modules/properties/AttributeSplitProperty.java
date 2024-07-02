package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

public class AttributeSplitProperty implements ModuleProperty {
    public static String KEY = "attribute_split";

    public AttributeSplitProperty() {
        AttributeProperty.attributeTransformers.add((oldMap, itemstack) -> {
            Multimap<Attribute, AttributeProperty.EntityAttributeModifierHolder> map = ArrayListMultimap.create(oldMap);
            Map<Context, List<SplitContext>> replaceMap = getMap(itemstack);
            for (Map.Entry<Context, List<SplitContext>> entry : replaceMap.entrySet()) {
                Attribute currentAttribute = entry.getKey().entityAttribute();
                EquipmentSlot equipmentSlot = entry.getKey().target();
                List<SplitContext> ratios = entry.getValue();

                if (!map.containsKey(currentAttribute)) {
                    continue;
                }

                Collection<AttributeProperty.EntityAttributeModifierHolder> list = oldMap.get(currentAttribute);

                double totalValue = list.stream()
                        .filter(attributeEntry -> attributeEntry.mergeTo().equals(AttributeModifier.Operation.ADD_VALUE))
                        .filter(attributeEntry -> attributeEntry.slot().equals(equipmentSlot))
                        .mapToDouble(entityAttributeModifierHolder -> entityAttributeModifierHolder.attributeModifier().amount())
                        .sum();
                ratios.forEach(((entityAttribute) -> {
                    Collection<AttributeProperty.EntityAttributeModifierHolder> foundAttributes = oldMap.get(entityAttribute.entityAttribute());

                    double baseValue = 0.0;

                    ResourceLocation id = AttributeProperty.getIDforSlot(equipmentSlot, entityAttribute.entityAttribute(), AttributeModifier.Operation.ADD_VALUE, "miapi:attribute_split");

                    if (foundAttributes != null && !foundAttributes.isEmpty()) {
                        Optional<AttributeProperty.EntityAttributeModifierHolder> holder = foundAttributes.stream()
                                .filter(attributeEntry -> attributeEntry.mergeTo().equals(AttributeModifier.Operation.ADD_VALUE))
                                .filter(attributeEntry -> attributeEntry.slot().equals(equipmentSlot))
                                .findFirst();

                        if (holder.isPresent()) {
                            baseValue = holder.get().attributeModifier().amount();
                            id = holder.get().attributeModifier().id();
                            map.remove(entityAttribute.entityAttribute(), holder.get());
                        }
                    }

                    double value = baseValue + totalValue * entityAttribute.percent;
                    if (value != 0) {
                        map.put(
                                entityAttribute.entityAttribute(),
                                new AttributeProperty.EntityAttributeModifierHolder(
                                        new AttributeModifier(id, baseValue + totalValue * entityAttribute.percent, AttributeModifier.Operation.ADD_VALUE),
                                        equipmentSlot,
                                        AttributeModifier.Operation.ADD_VALUE
                                ));
                    }
                }));

            }

            return map;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    public Map<Context, List<SplitContext>> getMap(ItemStack itemStack) {
        Map<Context, List<SplitContext>> finishedMap = new HashMap<>();
        ItemModule.getModules(itemStack).allSubModules().forEach(moduleInstance -> {
            Map<Context, List<SplitContext>> partMap = getMap(moduleInstance);
            partMap.forEach((key, entryList) -> {
                if (finishedMap.containsKey(key)) {

                    List<SplitContext> merge = new ArrayList<>(finishedMap.get(key));
                    merge.addAll(entryList);
                    finishedMap.put(key, merge);
                } else {
                    finishedMap.put(key, entryList);
                }
            });
        });
        return finishedMap;
    }

    public Map<Context, List<SplitContext>> getMap(ModuleInstance moduleInstance) {
        JsonElement element = moduleInstance.getOldProperties().get(this);
        if (element != null) {
            return getMap(element, moduleInstance);
        }
        return new HashMap<>();
    }

    public Map<Context, List<SplitContext>> getMap(JsonElement jsonElement, ModuleInstance moduleInstance) {
        JsonObject object = jsonElement.getAsJsonObject();
        Map<Context, List<SplitContext>> contextListMap = new HashMap<>();
        object.asMap().forEach((attributeKey, innerJson) -> {
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeKey));
            if (attribute == null) {
                Miapi.LOGGER.info("could not find attribute " + attributeKey);
            } else {
                innerJson.getAsJsonObject().asMap().forEach((slotKey, data) -> {
                    try {
                        EquipmentSlot equipmentSlot = EquipmentSlot.byName(slotKey);
                        List<SplitContext> splitContexts = data.getAsJsonArray().asList().stream().map(json -> {
                            String key = json.getAsJsonObject().get("attribute").getAsString();
                            Attribute targetAttribute = BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(key));
                            if (targetAttribute == null) {
                                Miapi.LOGGER.info("could not find attribute " + attributeKey);
                                return null;
                            }
                            double percent = ModuleProperty.getDouble(json.getAsJsonObject(), "percentage", moduleInstance, 1.0);
                            return new SplitContext(targetAttribute, (float) percent);
                        }).filter(Objects::nonNull).toList();
                        contextListMap.put(new Context(attribute, equipmentSlot), splitContexts);
                    } catch (IllegalArgumentException illegalArgumentException) {
                        Miapi.LOGGER.info("Slot not found in Attributesplitproperty " + slotKey);
                    }
                });
            }
        });
        return contextListMap;
    }

    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (old != null && toMerge != null) {
            Map<String, JsonElement> elementMap = new HashMap<>();
            Miapi.LOGGER.info(Miapi.gson.toJson(old));
            Miapi.LOGGER.info(Miapi.gson.toJson(toMerge));
            old.getAsJsonObject().asMap().forEach((key, data) -> {
                if (elementMap.containsKey(key)) {
                    JsonElement element = elementMap.get(key);
                    elementMap.put(key, mergeAttributeTogether(element, data));
                } else {
                    elementMap.put(key, data);
                }
            });
            toMerge.getAsJsonObject().asMap().forEach((key, data) -> {
                if (elementMap.containsKey(key)) {
                    JsonElement element = elementMap.get(key);
                    elementMap.put(key, mergeAttributeTogether(element, data));
                } else {
                    elementMap.put(key, data);
                }
            });
            JsonElement element = mergeAsMapTogether(old, toMerge, this::mergeAttributeTogether);
            Miapi.LOGGER.info(Miapi.gson.toJson(element));
            return element;
        }
        if (MergeType.EXTEND == type) {
            return old;
        } else {
            return toMerge;
        }
    }

    JsonElement mergeAttributeTogether(JsonElement element, JsonElement other) {
        return mergeAsMapTogether(element, other, (left, right) -> mergeTogether(element, other));
    }

    JsonElement mergeTogether(JsonElement element, JsonElement other) {
        Map<String, String> map = new HashMap<>();
        element.getAsJsonArray().forEach(element1 -> {
            String itemId = element1.getAsJsonObject().get("attribute").getAsString();
            String asd = element1.getAsJsonObject().get("percentage").getAsString();
            if (map.containsKey(itemId)) {
                map.put(itemId, asd + "+" + map.get(itemId));
            } else {
                map.put(itemId, asd);
            }
        });

        other.getAsJsonArray().forEach(element1 -> {
            String itemId = element1.getAsJsonObject().get("attribute").getAsString();
            String asd = element1.getAsJsonObject().get("percentage").getAsString();
            if (map.containsKey(itemId)) {
                map.put(itemId, asd + "+" + map.get(itemId));
            } else {
                map.put(itemId, asd);
            }
        });

        JsonObject object = new JsonObject();
        map.forEach(object::addProperty);

        return object;
    }

    JsonElement mergeAsMapTogether(JsonElement old, JsonElement other, CollisionMerge collisionMerge) {
        JsonObject object = new JsonObject();
        old.getAsJsonObject().asMap().forEach((key, data) -> {
            if (object.has(key)) {
                JsonElement element = object.get(key);
                object.add(key, collisionMerge.merge(element, data));
            } else {
                object.add(key, data);
            }
        });
        other.getAsJsonObject().asMap().forEach((key, data) -> {
            if (object.has(key)) {
                JsonElement element = object.get(key);
                object.add(key, collisionMerge.merge(element, data));
            } else {
                object.add(key, data);
            }
        });
        return object;
    }

    public interface CollisionMerge {
        JsonElement merge(JsonElement left, JsonElement right);
    }

    public record SplitContext(Attribute entityAttribute, Float percent) {
    }

    public record Context(Attribute entityAttribute, EquipmentSlot target) {
    }
}
