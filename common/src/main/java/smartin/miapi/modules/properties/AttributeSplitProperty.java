package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

public class AttributeSplitProperty implements ModuleProperty {
    public static String KEY = "attribute_split";

    public AttributeSplitProperty() {
        AttributeProperty.attributeTransformers.add((oldMap, itemstack) -> {
            Multimap<EntityAttribute, AttributeProperty.EntityAttributeModifierHolder> map = ArrayListMultimap.create(oldMap);
            Map<Context, List<SplitContext>> replaceMap = getMap(itemstack);
            for (Map.Entry<Context, List<SplitContext>> entry : replaceMap.entrySet()) {
                EntityAttribute currentAttribute = entry.getKey().entityAttribute();
                EquipmentSlot equipmentSlot = entry.getKey().target();
                List<SplitContext> ratios = entry.getValue();

                if (!map.containsKey(currentAttribute)) {
                    continue;
                }

                Collection<AttributeProperty.EntityAttributeModifierHolder> list = oldMap.get(currentAttribute);

                double totalValue = list.stream()
                        .filter(attributeEntry -> attributeEntry.mergeTo().equals(EntityAttributeModifier.Operation.ADDITION))
                        .filter(attributeEntry -> attributeEntry.slot().equals(equipmentSlot))
                        .mapToDouble(entityAttributeModifierHolder -> entityAttributeModifierHolder.attributeModifier().getValue())
                        .sum();
                ratios.forEach(((entityAttribute) -> {
                    Collection<AttributeProperty.EntityAttributeModifierHolder> foundAttributes = oldMap.get(entityAttribute.entityAttribute());

                    double baseValue = 0.0;

                    UUID uuid = AttributeProperty.getUUIDForSlot(equipmentSlot, EntityAttributeModifier.Operation.ADDITION, "miapi:attribute_split");

                    if (foundAttributes != null && !foundAttributes.isEmpty()) {
                        Optional<AttributeProperty.EntityAttributeModifierHolder> holder = foundAttributes.stream()
                                .filter(attributeEntry -> attributeEntry.mergeTo().equals(EntityAttributeModifier.Operation.ADDITION))
                                .filter(attributeEntry -> attributeEntry.slot().equals(equipmentSlot))
                                .findFirst();

                        if (holder.isPresent()) {
                            baseValue = holder.get().attributeModifier().getValue();
                            uuid = holder.get().attributeModifier().getId();
                            map.remove(entityAttribute.entityAttribute(), holder.get());
                        }
                    }

                    double value = baseValue + totalValue * entityAttribute.percent;
                    if (value != 0) {
                        map.put(
                                entityAttribute.entityAttribute(),
                                new AttributeProperty.EntityAttributeModifierHolder(
                                        new EntityAttributeModifier(uuid, "miapi:attribute_split", baseValue + totalValue * entityAttribute.percent, EntityAttributeModifier.Operation.ADDITION),
                                        equipmentSlot,
                                        false,
                                        EntityAttributeModifier.Operation.ADDITION
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

    public Map<Context, List<SplitContext>> getMap(ItemModule.ModuleInstance moduleInstance) {
        JsonElement element = moduleInstance.getProperties().get(this);
        if (element != null) {
            return getMap(element, moduleInstance);
        }
        return new HashMap<>();
    }

    public Map<Context, List<SplitContext>> getMap(JsonElement jsonElement, ItemModule.ModuleInstance moduleInstance) {
        JsonObject object = jsonElement.getAsJsonObject();
        Map<Context, List<SplitContext>> contextListMap = new HashMap<>();
        object.asMap().forEach((attributeKey, innerJson) -> {
            EntityAttribute attribute = Registries.ATTRIBUTE.get(new Identifier(attributeKey));
            if (attribute == null) {
                Miapi.LOGGER.info("could not find attribute " + attributeKey);
            } else {
                innerJson.getAsJsonObject().asMap().forEach((slotKey, data) -> {
                    try {
                        EquipmentSlot equipmentSlot = EquipmentSlot.byName(slotKey);
                        List<SplitContext> splitContexts = data.getAsJsonArray().asList().stream().map(json -> {
                            String key = json.getAsJsonObject().get("attribute").getAsString();
                            EntityAttribute targetAttribute = Registries.ATTRIBUTE.get(new Identifier(key));
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

    public record SplitContext(EntityAttribute entityAttribute, Float percent) {
    }

    public record Context(EntityAttribute entityAttribute, EquipmentSlot target) {
    }
}
