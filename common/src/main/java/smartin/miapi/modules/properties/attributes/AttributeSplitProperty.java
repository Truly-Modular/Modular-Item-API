package smartin.miapi.modules.properties.attributes;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * "minecraft:generic.attack_damage": {
 * "mainhand": [
 * {
 * "attribute": "minecraft:generic.attack_damage",
 * "percentage": "20",
 * "target":"offhand"
 * }
 * ]
 * }
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
    public static final Codec<Map<ResourceLocation, Map<EquipmentSlotGroup, List<ActualInner>>>> ACUTAL_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC,
            Codec.unboundedMap(
                    EquipmentSlotGroup.CODEC,
                    ActualInner.CODEC.listOf()
            )
    );

    public static final Codec<Map<AttributeSplitProperty.Context, List<AttributeSplitProperty.SplitContext>>> CODEC = ACUTAL_CODEC.xmap(map -> {
        Map<AttributeSplitProperty.Context, List<AttributeSplitProperty.SplitContext>> finishedMap = new HashMap<>();
        map.forEach((id, innerMap) -> {
            innerMap.forEach((group, dataList) -> {
                dataList.forEach(data -> {
                    Context context = new Context(id, group);
                    finishedMap.computeIfAbsent(context, (c) -> new ArrayList<>()).add(new SplitContext(data.targetAttribute(), data.percent, data.target, null));
                });
            });
        });
        return finishedMap;
    }, inner ->
    {
        Map<ResourceLocation, Map<EquipmentSlotGroup, List<ActualInner>>> finishedMap = new HashMap<>();
        inner.forEach((context, split) -> {
            split.forEach(splitContext -> {
                finishedMap
                        .computeIfAbsent(context.entityAttribute(), (c) -> new HashMap<>())
                        .computeIfAbsent(context.target(), (e) -> new ArrayList<>()).add(new ActualInner(splitContext.entityAttribute(), splitContext.percent(), splitContext.target()));
            });
        });
        return finishedMap;
    });

    @Override
    public Map<Context, List<SplitContext>> initialize(Map<Context, List<SplitContext>> property, ModuleInstance context) {
        Map<Context, List<SplitContext>> map = new HashMap<>();
        property.forEach((attributeContext, list) -> {
            List<SplitContext> newList = list.stream().map(splitContext -> new SplitContext(splitContext.entityAttribute(), splitContext.percent().initialize(context), splitContext.target, context)).toList();
            map.put(attributeContext, newList);
        });
        return map;
    }

    public AttributeSplitProperty() {
        super(CODEC);
        AttributeUtil.ITEM_ATTRIBUTE_ADJUST.register((attributeContext, itemStack) -> {
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> map = new HashMap<>(attributeContext.map);
            Map<Context, List<SplitContext>> replaceMap = getData(itemStack).orElse(new HashMap<>());

            for (Map.Entry<Context, List<SplitContext>> entry : replaceMap.entrySet()) {
                EquipmentSlotGroup equipmentSlot = entry.getKey().target();
                List<SplitContext> ratios = entry.getValue();

                ResourceLocation attributeKey;
                if (AttributePropertyRework.replaceMap.containsKey(entry.getKey().entityAttribute().toString())) {
                    attributeKey = BuiltInRegistries.ATTRIBUTE.getKey(AttributePropertyRework.replaceMap.get(entry.getKey().entityAttribute().toString()).get());
                } else {
                    attributeKey = entry.getKey().entityAttribute();
                }
                if (!map.containsKey(attributeKey)) {
                    continue;
                }

                Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>> operationMap = map.get(attributeKey);
                Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable> addValueMap = operationMap.get(AttributeModifier.Operation.ADD_VALUE);
                if (addValueMap == null) {
                    continue;
                }

                double totalValue = addValueMap.entrySet().stream()
                        .filter(entrySet -> entrySet.getKey().left().isPresent() && entrySet.getKey().left().get().equals(equipmentSlot))
                        .mapToDouble(entrySet -> entrySet.getValue().getValue())
                        .sum();

                for (SplitContext splitContext : ratios) {
                    EquipmentSlotGroup targetGroup = splitContext.target() == null ? equipmentSlot : splitContext.target();
                    Either<EquipmentSlotGroup, Boolean> targetKey = Either.left(targetGroup);

                    var resolveAble = addValueMap.get(targetKey);
                    if (resolveAble != null) {
                        var operation = new DoubleOperationResolvable.Operation(totalValue * splitContext.percent().getValue() / 100.0, AttributeModifier.Operation.ADD_VALUE);
                        operation.instance = splitContext.moduleInstance;
                        List<DoubleOperationResolvable.Operation> operations = new ArrayList<>(resolveAble.operations);
                        operations.add(operation);
                        resolveAble.operations = operations;
                        resolveAble.clearCache();
                        resolveAble.getValue();
                    } else {
                        resolveAble = new DoubleOperationResolvable(List.of(new DoubleOperationResolvable.Operation(totalValue * splitContext.percent().getValue() / 100.0, AttributeModifier.Operation.ADD_VALUE)));
                        resolveAble = resolveAble.initialize(splitContext.moduleInstance);
                        addValueMap.put(targetKey, resolveAble);
                    }
                }
            }
            return EventResult.pass();
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

    public record SplitContext(ResourceLocation entityAttribute,
                               DoubleOperationResolvable percent,
                               @Nullable EquipmentSlotGroup target,
                               @Nullable ModuleInstance moduleInstance) {
    }

    public record Context(ResourceLocation entityAttribute, EquipmentSlotGroup target) {
    }

    public record ActualInner(ResourceLocation targetAttribute, DoubleOperationResolvable percent,
                              EquipmentSlotGroup target) {
        public static final Codec<ActualInner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("attribute").forGetter(ActualInner::targetAttribute),
                DoubleOperationResolvable.CODEC.fieldOf("percentage").forGetter(ActualInner::percent),
                EquipmentSlotGroup.CODEC.fieldOf("target").forGetter(ActualInner::target)
        ).apply(instance, ActualInner::new));
    }

}
