package smartin.miapi.modules.properties.attributes;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.armor.EquipmentSlotProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;
import java.util.function.Supplier;

public class AttributeProperty extends
        CodecProperty<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>>
        implements ComponentApplyProperty, SourceSetter<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>> {
    public static final ResourceLocation KEY = Miapi.id("attributes");
    public static AttributeProperty property;
    public static final Map<String, Supplier<Attribute>> replaceMap = new HashMap<>();
    public static final Map<Attribute, Float> priorityMap = new HashMap<>();
    public static Codec<List<AttributeJson>> OLD_CODEC = Codec.list(AutoCodec.of(AttributeJson.class).codec());
    public static Codec<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>> NEW_CODEC =
            Codec.unboundedMap(
                    ResourceLocation.CODEC.xmap(id -> {
                        if (replaceMap.containsKey(id.toString())) {
                            return BuiltInRegistries.ATTRIBUTE.getKey(replaceMap.get(id.toString()).get());
                        }
                        return id;
                    }, id -> id),
                    Codec.unboundedMap(
                            AttributeModifier.Operation.CODEC,
                            Codec.unboundedMap(
                                    EquipmentSlotGroupWrapper.CODEC,
                                    DoubleOperationResolvable.CODEC)));
    public static Codec<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>> CODEC = Codec.withAlternative(NEW_CODEC,
            OLD_CODEC.xmap(list -> {
                Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> map = new LinkedHashMap<>();
                list.forEach(attributeJson -> {
                    ResourceLocation id;
                    if (replaceMap.containsKey(attributeJson.attribute)) {
                        id = BuiltInRegistries.ATTRIBUTE.getKey(replaceMap.get(attributeJson.attribute).get());
                    } else {
                        id = ResourceLocation.parse(attributeJson.attribute);
                    }
                    DoubleOperationResolvable.IndividualOperation.Operation operation = DoubleOperationResolvable.IndividualOperation.getOperation(attributeJson.operation);
                    AttributeModifier.Operation targetOperation = getOperation(attributeJson.targetOperation == null ? "+" : attributeJson.targetOperation);
                    EquipmentSlotGroupWrapper equipmentSlotGroup = attributeJson.slot;
                    DoubleOperationResolvable.IndividualOperation doubleOperation = new DoubleOperationResolvable.IndividualOperation(attributeJson.value);
                    if (targetOperation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                        if (operation.equals(DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_BASE)) {
                            operation = DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE;
                        } else {
                            operation = DoubleOperationResolvable.IndividualOperation.Operation.ADD_MULTIPLIED_TOTAL;
                        }
                    }
                    if (targetOperation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                        operation = DoubleOperationResolvable.IndividualOperation.Operation.ADD_VALUE;
                    }
                    doubleOperation.attributeOperation = operation;

                    map.computeIfAbsent(id, i -> new LinkedHashMap<>())
                            .computeIfAbsent(targetOperation, t -> new LinkedHashMap<>())
                            .compute(equipmentSlotGroup, (e, resolvable1) -> {
                                if (resolvable1 == null) {
                                    return new DoubleOperationResolvable(List.of(doubleOperation));
                                }
                                List<DoubleOperationResolvable.IndividualOperation> operations = new ArrayList<>(resolvable1.operations);
                                operations.add(doubleOperation);
                                return new DoubleOperationResolvable(operations);
                            });
                });
                return map;
            }, map -> List.of()));

    public static AttributeModifier.Operation getOperation(String operationString) {
        return switch (operationString) {
            case "*" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "**" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    public AttributeProperty() {
        super(CODEC);
        property = this;
        priorityMap.put(Attributes.ARMOR.value(), -15.0f);
        priorityMap.put(Attributes.ARMOR_TOUGHNESS.value(), -14.0f);
        priorityMap.put(Attributes.KNOCKBACK_RESISTANCE.value(), -13.0f);
        priorityMap.put(Attributes.ATTACK_DAMAGE.value(), -12.0f);
        priorityMap.put(AttributeRegistry.MAGIC_DAMAGE.value(), -11.5f);
        priorityMap.put(Attributes.ATTACK_SPEED.value(), -11.0f);
        priorityMap.put(AttributeRegistry.CRITICAL_DAMAGE.value(), -10.9f);
        priorityMap.put(AttributeRegistry.CRITICAL_CHANCE.value(), -10.8f);
        priorityMap.put(AttributeRegistry.PROJECTILE_DAMAGE.value(), -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_SPEED.value(), -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_ACCURACY.value(), -9.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_PIERCING.value(), -9.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_AXE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_PICKAXE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_HOE.value(), -8.0f);
        priorityMap.put(AttributeRegistry.MINING_SPEED_SHOVEL.value(), -8.0f);
        priorityMap.put(Attributes.BLOCK_INTERACTION_RANGE.value(), -7.0f);
        priorityMap.put(Attributes.ENTITY_INTERACTION_RANGE.value(), -7.0f);
        priorityMap.put(AttributeRegistry.BACK_STAB.value(), -6.0f);
        priorityMap.put(AttributeRegistry.SHIELD_BREAK.value(), -6.0f);
        priorityMap.put(AttributeRegistry.ARMOR_CRUSHING.value(), -6.0f);

        AttributeProperty.replaceMap.put("miapi:generic.reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", Attributes.ENTITY_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("forge:block_reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("forge:entity_reach", Attributes.ENTITY_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", Attributes.BLOCK_INTERACTION_RANGE::value);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", Attributes.ENTITY_INTERACTION_RANGE::value);
        ModularItemCache.setSupplier(KEY + "miapi_attribute_list", AttributeProperty::buildMiapiModifiersCache);
    }

    public void updateComponentold(ItemStack itemStack, RegistryAccess registryAccess) {
        var attributes = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        Map<ResourceLocation, ItemAttributeModifiers.Entry> mergedEntries = new HashMap<>();

        for (var entry : attributes.modifiers()) {
            var id = entry.modifier().id();
            if (!(id.getNamespace().equals(Miapi.MOD_ID)
                  || id.equals(Item.BASE_ATTACK_DAMAGE_ID)
                  || id.equals(Item.BASE_ATTACK_SPEED_ID))) {
                mergedEntries.put(id, entry);
            }
        }

        getData(itemStack).ifPresent(idMap -> {
            AttributeUtil.AttributeContext context = new AttributeUtil.AttributeContext();
            context.map = idMap;
            AttributeUtil.ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, itemStack);
            idMap = context.map;

            for (var entry : idMap.entrySet()) {
                Attribute attribute = findAttribute(entry.getKey());
                if (attribute == null) continue;

                for (var opEntry : entry.getValue().entrySet()) {
                    Map<EquipmentSlotGroup, DoubleOperationResolvable> resolvableMap = new HashMap<>();
                    AttributeModifier.Operation operation = opEntry.getKey();

                    for (var slotEntry : opEntry.getValue().entrySet()) {
                        EquipmentSlotGroupWrapper slot = slotEntry.getKey();
                        DoubleOperationResolvable expression = slotEntry.getValue();

                        // Determine effective slot
                        EquipmentSlotGroup slotGroup = EquipmentSlotProperty.getSlot(itemStack);
                        if (slot.group().isPresent()) {
                            resolvableMap.merge(slot.group.get(), expression, (a, b) -> DoubleOperationResolvable.merge(a, b, MergeType.SMART));
                        } else if (slot.raw().equals("slot")) {
                            resolvableMap.merge(EquipmentSlotProperty.getSlot(itemStack), expression, (a, b) -> DoubleOperationResolvable.merge(a, b, MergeType.SMART));
                        }
                    }
                    resolvableMap.forEach((group, op) -> {
                        ResourceLocation slotId = AttributeUtil.getIDForSlot(group, attribute, operation);
                        op.clearCache();
                        double value = op.evaluate(attribute.getDefaultValue()).orElse(0.0)
                                       - attribute.getDefaultValue();
                        mergedEntries.put(
                                slotId,
                                new ItemAttributeModifiers.Entry(
                                        BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute),
                                        new AttributeModifier(slotId, value, operation),
                                        group
                                )
                        );
                    });
                }
            }
        });

        List<ItemAttributeModifiers.Entry> finalList = new ArrayList<>(mergedEntries.values());
        AttributeUtil.ItemVanillaAttributeContext context = new AttributeUtil.ItemVanillaAttributeContext();
        context.list = finalList;
        AttributeUtil.VANILLA_ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, itemStack);
        context.list.sort(Comparator.comparingDouble(e -> priorityMap.getOrDefault(e.attribute().value(), 0.0f)));

        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(context.list, true));
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        List<ItemAttributeModifiers.Entry> finalList = new ArrayList<>();
        buildMiapiModifiers(itemStack).forEach(miapiAttributeModifier -> {
            miapiAttributeModifier.slot.group().ifPresent(group -> {
                finalList.add(new ItemAttributeModifiers.Entry(
                        BuiltInRegistries.ATTRIBUTE.wrapAsHolder(miapiAttributeModifier.attribute),
                        new AttributeModifier(
                                miapiAttributeModifier.id,
                                miapiAttributeModifier.value.getValue(),
                                miapiAttributeModifier.operation
                        ),
                        group
                ));
            });
        });
        AttributeUtil.ItemVanillaAttributeContext context = new AttributeUtil.ItemVanillaAttributeContext();
        AttributeUtil.VANILLA_ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, itemStack);
        context.list = finalList;
        context.list.sort(Comparator.comparingDouble(e -> priorityMap.getOrDefault(e.attribute().value(), 0.0f)));
        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(context.list, true));
    }

    public static List<MiapiAttributeModifier> buildMiapiModifiers(ItemStack stack) {
        return ModularItemCache.get(stack,KEY + "miapi_attribute_list",List.of());
    }

    private static List<MiapiAttributeModifier> buildMiapiModifiersCache(ItemStack stack) {
        List<MiapiAttributeModifier> result = new ArrayList<>();

        var optional = property.getData(stack);
        if (optional.isEmpty()) return result;

        var originalMap = optional.get();

        // 1. Split maps
        Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> explicitMap = new HashMap<>();
        var slotMap = new HashMap<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>();

        for (var attrEntry : originalMap.entrySet()) {
            for (var opEntry : attrEntry.getValue().entrySet()) {
                for (var slotEntry : opEntry.getValue().entrySet()) {
                    EquipmentSlotGroupWrapper slot = slotEntry.getKey();

                    var target = ("slot".equals(slot.raw()) && slot.group().isEmpty())
                            ? slotMap
                            : explicitMap;

                    target
                            .computeIfAbsent(attrEntry.getKey(), k -> new HashMap<>())
                            .computeIfAbsent(opEntry.getKey(), k -> new HashMap<>())
                            .put(slot, slotEntry.getValue());
                }
            }
        }

        // 2. Resolve "slot" entries
        EquipmentSlotGroup resolved = EquipmentSlotProperty.getSlot(stack);
        if (resolved != null) {
            var resolvedSlotMap = new HashMap<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>>();

            for (var attrEntry : slotMap.entrySet()) {
                for (var opEntry : attrEntry.getValue().entrySet()) {
                    for (var slotEntry : opEntry.getValue().entrySet()) {
                        EquipmentSlotGroupWrapper wrapped =
                                new EquipmentSlotGroupWrapper(resolved.getSerializedName(), Optional.of(resolved));

                        resolvedSlotMap
                                .computeIfAbsent(attrEntry.getKey(), k -> new HashMap<>())
                                .computeIfAbsent(opEntry.getKey(), k -> new HashMap<>())
                                .put(wrapped, slotEntry.getValue());
                    }
                }
            }

            explicitMap = property.merge(explicitMap, resolvedSlotMap, MergeType.SMART);
        }

        AttributeUtil.AttributeContext context = new AttributeUtil.AttributeContext();
        context.map = explicitMap;
        AttributeUtil.ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, stack);
        var finalMap = context.map;

        // 5. Build result
        for (var entry : finalMap.entrySet()) {
            Attribute attribute = findAttribute(entry.getKey());
            if (attribute == null) continue;

            for (var opEntry : entry.getValue().entrySet()) {
                AttributeModifier.Operation operation = opEntry.getKey();

                for (var slotEntry : opEntry.getValue().entrySet()) {
                    EquipmentSlotGroupWrapper slot = slotEntry.getKey();
                    DoubleOperationResolvable expression = slotEntry.getValue();

                    result.add(new MiapiAttributeModifier(
                            AttributeUtil.getIDForSlot(
                                    slot.group().orElse(EquipmentSlotGroup.MAINHAND),
                                    attribute,
                                    operation
                            ),
                            attribute,
                            operation,
                            slot,
                            expression
                    ));
                }
            }
        }

        return result;
    }

    public static Attribute findAttribute(ResourceLocation id) {
        var replacement = replaceMap.get(id.toString());
        if (replacement != null) {
            return replacement.get();
        }
        return BuiltInRegistries.ATTRIBUTE.get(id);
    }

    @Override
    public Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> merge(
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> left,
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> right,
            MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (id, leftMap, rightMap) -> {
            return MergeAble.mergeMap(leftMap, rightMap, mergeType, (operation, leftOperationMap, rightOperationMap) -> {
                return MergeAble.mergeMap(leftOperationMap, rightOperationMap, mergeType, (slot, leftJson, rightJson) -> {
                    return DoubleOperationResolvable.merge(leftJson, rightJson, mergeType);
                });
            });
        });
    }

    public Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> initialize(Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> map, ModuleInstance moduleInstance) {
        Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> init = new LinkedHashMap<>();
        map.forEach((id, attributeOpMap) -> {
            attributeOpMap.forEach((op, groupMap) -> {
                groupMap.forEach((slot, resolveAble) -> {
                    init.computeIfAbsent(id,
                            (s) -> new LinkedHashMap<>()).computeIfAbsent(op,
                            (a) -> new LinkedHashMap<>()).computeIfAbsent(slot,
                            (b) -> resolveAble.initialize(moduleInstance));
                });
            });
        });
        AttributeUtil.AttributeContext context = new AttributeUtil.AttributeContext();
        context.map = init;
        AttributeUtil.MODULE_ATTRIBUTE_ADJUST.invoker().adjust(context, moduleInstance);
        return context.map;
    }

    @Override
    public Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> setSource(
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> map, Component source) {
        Map<ResourceLocation, Map<AttributeModifier.Operation, Map<EquipmentSlotGroupWrapper, DoubleOperationResolvable>>> init = new LinkedHashMap<>();
        map.forEach((id, attributeOpMap) -> {
            attributeOpMap.forEach((op, groupMap) -> {
                groupMap.forEach((slot, resolveAble) -> {
                    init.computeIfAbsent(id,
                            (s) -> new LinkedHashMap<>()).computeIfAbsent(op,
                            (a) -> new LinkedHashMap<>()).computeIfAbsent(slot,
                            (b) -> resolveAble.setSource(resolveAble, source));
                });
            });
        });
        AttributeUtil.AttributeContext context = new AttributeUtil.AttributeContext();
        context.map = init;
        return context.map;
    }

    public static class AttributeJson {
        public static Codec<String> VALUE_CODEC = Codec.withAlternative(
                Codec.STRING,
                Codec.DOUBLE.xmap(i -> "" + i, b -> 0.0));

        public String attribute;
        @CodecBehavior.Override("VALUE_CODEC")
        public String value;
        public String operation;
        @CodecBehavior.Optional
        public EquipmentSlotGroupWrapper slot = new EquipmentSlotGroupWrapper("slot");
        @CodecBehavior.Optional
        @AutoCodec.Name("target_operation")
        public String targetOperation;
    }

    public record MiapiAttributeModifier(
            ResourceLocation id,
            Attribute attribute,
            AttributeModifier.Operation operation,
            EquipmentSlotGroupWrapper slot,
            DoubleOperationResolvable value
    ) {
    }
}
