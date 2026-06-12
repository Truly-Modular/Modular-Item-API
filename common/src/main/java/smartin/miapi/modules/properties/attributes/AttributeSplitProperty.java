package smartin.miapi.modules.properties.attributes;

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
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> map = attributeContext.map;
            Map<Context, List<SplitContext>> replaceMap = getData(itemStack).orElse(new HashMap<>());

            for (Map.Entry<Context, List<SplitContext>> entry : replaceMap.entrySet()) {
                EquipmentSlotGroup equipmentSlot = entry.getKey().target();
                List<SplitContext> ratios = entry.getValue();

                ResourceLocation attributeKey;
                if (AttributeProperty.replaceMap.containsKey(entry.getKey().entityAttribute().toString())) {
                    attributeKey = BuiltInRegistries.ATTRIBUTE.getKey(AttributeProperty.replaceMap.get(entry.getKey().entityAttribute().toString()).get());
                } else {
                    attributeKey = entry.getKey().entityAttribute();
                }
                if (!map.containsKey(attributeKey)) {
                    continue;
                }

                Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>> operationMap = map.get(attributeKey);
                Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable> addValueMap = operationMap.get(AttributeModifier.Operation.ADD_VALUE);
                if (addValueMap == null) {
                    continue;
                }

                double totalValue = addValueMap.entrySet().stream()
                        .filter(entrySet -> entrySet.getKey().group().isPresent() && entrySet.getKey().group().get().equals(equipmentSlot))
                        .mapToDouble(entrySet -> entrySet.getValue().getValue())
                        .sum();

                for (SplitContext splitContext : ratios) {
                    EquipmentSlotGroup targetGroup = splitContext.target() == null ? equipmentSlot : splitContext.target();
                    EquipmentSlotGroupWrapper equipmentSlotGroupWrapper = new EquipmentSlotGroupWrapper(targetGroup);

                    Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>> operationMapTarget = map.computeIfAbsent(splitContext.entityAttribute,(i)-> new HashMap<>());
                    Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable> addValueMapTarget = operationMapTarget.computeIfAbsent(AttributeModifier.Operation.ADD_VALUE,(i)-> new HashMap<>());
                    if (addValueMap == null) {
                        continue;
                    }

                    var resolveAble = addValueMapTarget.get(equipmentSlotGroupWrapper);
                    if (resolveAble != null) {
                        var operation = new DoubleOperationResolvable.IndividualOperation(totalValue * splitContext.percent().getValue() / 100.0, DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE.ADD_VALUE);
                        operation.instance = splitContext.moduleInstance;
                        List<DoubleOperationResolvable.IndividualOperation> operations = new ArrayList<>(resolveAble.operations);
                        operations.add(operation);
                        resolveAble.operations = operations;
                        resolveAble.clearCache();
                        resolveAble.getValue();
                    } else {
                        resolveAble = new DoubleOperationResolvable(List.of(new DoubleOperationResolvable.IndividualOperation(totalValue * splitContext.percent().getValue() / 100.0, DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE)));
                        resolveAble = resolveAble.initialize(splitContext.moduleInstance);
                        double value = resolveAble.getValue();
                        if (value != 0) {
                            addValueMapTarget.put(equipmentSlotGroupWrapper, resolveAble);
                        }
                    }
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public Map<Context, List<SplitContext>> merge(Map<Context, List<SplitContext>> left, Map<Context, List<SplitContext>> right, MergeType mergeType) {
        return MergeAble.mergeMap(left,right,mergeType,(c,l,r)-> MergeAble.mergeList(l,r,mergeType));
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
