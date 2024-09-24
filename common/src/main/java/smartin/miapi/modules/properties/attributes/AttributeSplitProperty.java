package smartin.miapi.modules.properties.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.*;

/**
 * This property allows modules to define and modify attribute splits for items.
 *
 * @header Attribute Split Property
 * @description_start The Attribute Split Property is used to divide attributes across different contexts.
 * It enables assigning attributes to multiple slots and merging them based on a percentage split.
 * This Doesnt by default remove the attribute, if you wish to remove the existing attribute you need to split that attribute in
 * with the percentage -100
 * This property is essential to re-balance certain attributes for usage with other mods like alembic.
 * This Example would give 20% of the weapons attack damage as offhand damage
 * ```json
 * "attribute_split": {
 *   "minecraft:generic.attack_damage": {
 *     "mainhand": [
 *       {
 *         "attribute": "minecraft:generic.attack_damage",
 *         "percentage": "20",
 *         "target":"offhand"
 *       }
 *     ]
 *   }
 * }
 * ```
 * @description_end
 * @path /data_types/properties/attributes/attribute_split
 * @data context: a map containing the attribute context and split configurations.
 * @data first map key is the Attribute to be split
 * @data inner map key is the SlotGroup that is used as source
 * @data attribute:the attribute to be added
 * @data percentage:how much of the attribute should be added, 100 means same amount, 0.0 means nothing
 * @data target:(Optional) a target slot overwriting the source, if not set uses source slot
 */

public class AttributeSplitProperty extends CodecProperty<Map<AttributeSplitProperty.Context, List<AttributeSplitProperty.SplitContext>>> {
    public static final ResourceLocation KEY = Miapi.id("attribute_split");
    public static Codec<Map<Context, List<SplitContext>>> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Map<Context, List<SplitContext>>, T>> decode(DynamicOps<T> ops, T input) {
            Map<Context, List<SplitContext>> map = new HashMap<>();
            ops.getMap(input).getOrThrow().entries().forEach(pair -> {
                ResourceLocation attributeID = ResourceLocation.CODEC.parse(ops, pair.getFirst()).getOrThrow();
                ops.getMap(pair.getSecond()).getOrThrow().entries().forEach(ttPair -> {
                    EquipmentSlotGroup group = EquipmentSlotGroup.CODEC.parse(ops, ttPair.getFirst()).getOrThrow();
                    Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(attributeID);
                    if (attribute == null) {
                        Miapi.LOGGER.error("could not find Attribute " + attributeID);
                    } else {
                        Context context = new Context(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), group);
                        ops.getList(ttPair.getSecond()).getOrThrow().accept(inner -> {
                            ResourceLocation replaceAttributeID = ResourceLocation.CODEC.decode(ops, ops.getMap(input).getOrThrow().get("attribute")).getOrThrow().getFirst();
                            StatResolver.DoubleFromStat doubleFromStat = StatResolver.DoubleFromStat.codec.decode(ops, ops.getMap(input).getOrThrow().get("attribute")).getOrThrow().getFirst();
                            Attribute replaceAttribute = BuiltInRegistries.ATTRIBUTE.get(replaceAttributeID);
                            if (replaceAttribute == null) {
                                Miapi.LOGGER.error("could not find Attribute " + replaceAttribute);
                            } else {
                                SplitContext splitContext = new SplitContext(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(replaceAttribute), doubleFromStat, 0.0, null);
                                List<SplitContext> list = map.computeIfAbsent(context, (c) -> new ArrayList<>());
                                list.add(splitContext);
                            }
                        });
                    }
                });
            });
            return DataResult.success(new Pair<>(map, input));
        }

        @Override
        public <T> DataResult<T> encode(Map<Context, List<SplitContext>> input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding properties is not fully supported");
        }
    };

    @Override
    public Map<Context, List<SplitContext>> initialize(Map<Context, List<SplitContext>> property, ModuleInstance context) {
        Map<Context, List<SplitContext>> map = new HashMap<>();
        property.forEach((attributeContext, list) -> {
            List<SplitContext> newList = list.stream().map(splitContext -> new SplitContext(splitContext.entityAttribute(), splitContext.percent(), splitContext.percent().evaluate(context) / 100.0, splitContext.target)).toList();
            map.put(attributeContext, newList);
        });
        return map;
    }

    public AttributeSplitProperty() {
        super(CODEC);
        AttributeProperty.attributeTransformers.add((oldMap, itemstack) -> {
            Multimap<Holder<Attribute>, AttributeProperty.EntityAttributeModifierHolder> map = ArrayListMultimap.create(oldMap);
            Map<Context, List<SplitContext>> replaceMap = getData(itemstack).orElse(new HashMap<>());
            for (Map.Entry<Context, List<SplitContext>> entry : replaceMap.entrySet()) {
                Holder<Attribute> currentAttribute = entry.getKey().entityAttribute();
                EquipmentSlotGroup equipmentSlot = entry.getKey().target();
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
                    EquipmentSlotGroup targetGroup = entityAttribute.target() == null ? equipmentSlot : entityAttribute.target();

                    ResourceLocation id = AttributeUtil.getIDForSlot(targetGroup, entityAttribute.entityAttribute().value(), AttributeModifier.Operation.ADD_VALUE, "miapi:attribute_split");

                    if (foundAttributes != null && !foundAttributes.isEmpty()) {
                        Optional<AttributeProperty.EntityAttributeModifierHolder> holder = foundAttributes.stream()
                                .filter(attributeEntry -> attributeEntry.mergeTo().equals(AttributeModifier.Operation.ADD_VALUE))
                                .filter(attributeEntry -> attributeEntry.slot().equals(targetGroup))
                                .findFirst();

                        if (holder.isPresent()) {
                            baseValue = holder.get().attributeModifier().amount();
                            id = holder.get().attributeModifier().id();
                            map.remove(entityAttribute.entityAttribute(), holder.get());
                        }
                    }

                    double value = baseValue + totalValue * entityAttribute.value();
                    if (value != 0) {
                        map.put(
                                entityAttribute.entityAttribute(),
                                new AttributeProperty.EntityAttributeModifierHolder(
                                        new AttributeModifier(id, baseValue + totalValue * entityAttribute.value(), AttributeModifier.Operation.ADD_VALUE),
                                        targetGroup,
                                        AttributeModifier.Operation.ADD_VALUE
                                ));
                    }
                }));

            }

            return map;
        });
    }

    @Override
    public Map<Context, List<SplitContext>> merge(Map<Context, List<SplitContext>> left, Map<Context, List<SplitContext>> right, MergeType mergeType) {
        Map<Context, List<SplitContext>> merged = new HashMap<>(left);
        right.forEach((context, splitContexts) -> {
            if (merged.containsKey(context)) {
                List<SplitContext> contexts = new ArrayList<>(merged.get(context));
                contexts.addAll(splitContexts);
                merged.put(context, contexts);
            } else {
                merged.put(context, splitContexts);
            }
        });
        return merged;
    }

    public record SplitContext(Holder<Attribute> entityAttribute, StatResolver.DoubleFromStat percent, double value,
                               @Nullable @CodecBehavior.Optional EquipmentSlotGroup target) {
    }

    public record Context(Holder<Attribute> entityAttribute, EquipmentSlotGroup target) {
    }
}
