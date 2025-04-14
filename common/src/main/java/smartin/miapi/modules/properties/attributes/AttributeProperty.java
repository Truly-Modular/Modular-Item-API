package smartin.miapi.modules.properties.attributes;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import smartin.miapi.modules.properties.armor.EquipmentSlotProperty;
import smartin.miapi.modules.properties.util.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * This property allows modules to define and modify attributes of items.
 *
 * @header Attribute Property
 * @description_start The Attribute Property is used to modify various attributes for items
 * It can set basic, multiply and resolve complex attributes. This is a core part of the api
 * @path /data_types/properties/attributes/item_attributes
 * @data attributes:a list of item attributes, allowing each for the following fields
 * @data attribute: the ID of the attribute
 * @data value: double resolvable
 * @data operation: the operation to execute, + * ** are allowed
 * @data slot: the target slot group
 * @data targetOperation : optional, the operation to be merged to
 */
public class AttributeProperty extends CodecProperty<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>>> implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("attributes");
    public static AttributeProperty property;
    public static final Map<String, Supplier<Attribute>> replaceMap = new HashMap<>();
    public static final Map<Attribute, Float> priorityMap = new HashMap<>();
    public static Codec<List<AttributeJson>> OLD_CODEC = Codec.list(AutoCodec.of(AttributeJson.class).codec());
    public static Codec<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>>> NEW_CODEC =
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
                                    Codec.either(EquipmentSlotGroup.CODEC, Codec.BOOL),
                                    DoubleOperationResolvable.CODEC)));

    public static Codec<Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>>> CODEC = Codec.withAlternative(NEW_CODEC,
            OLD_CODEC.xmap(list -> {
                Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> map = new LinkedHashMap<>();
                list.forEach(attributeJson -> {
                    ResourceLocation id;
                    if (replaceMap.containsKey(attributeJson.attribute)) {
                        id = BuiltInRegistries.ATTRIBUTE.getKey(replaceMap.get(attributeJson.attribute).get());
                    } else {
                        id = ResourceLocation.parse(attributeJson.attribute);
                    }
                    AttributeModifier.Operation operation = DoubleOperationResolvable.Operation.getOperation(attributeJson.operation);
                    AttributeModifier.Operation targetOperation = DoubleOperationResolvable.Operation.getOperation(attributeJson.targetOperation == null ? "+" : attributeJson.targetOperation);
                    EquipmentSlotGroup equipmentSlotGroup = attributeJson.slot;
                    DoubleOperationResolvable.Operation doubleOperation = new DoubleOperationResolvable.Operation(attributeJson.value);
                    if (targetOperation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                        if (operation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                            operation = AttributeModifier.Operation.ADD_VALUE;
                        } else {
                            operation = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                        }
                    }
                    if (targetOperation.equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                        operation = AttributeModifier.Operation.ADD_VALUE;
                    }
                    doubleOperation.attributeOperation = operation;

                    map.computeIfAbsent(id, i -> new LinkedHashMap<>())
                            .computeIfAbsent(targetOperation, t -> new LinkedHashMap<>())
                            .compute(Either.left(equipmentSlotGroup), (e, resolvable1) -> {
                                if (resolvable1 == null) {
                                    return new DoubleOperationResolvable(List.of(doubleOperation));
                                }
                                List<DoubleOperationResolvable.Operation> operations = new ArrayList<>(resolvable1.operations);
                                operations.add(doubleOperation);
                                return new DoubleOperationResolvable(operations);
                            });
                });
                return map;
            }, map -> List.of()));

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
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        var attributes = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> filteredList = new ArrayList<>(attributes.modifiers().stream().filter(
                entry -> !(entry.modifier().id().getNamespace().equals(Miapi.MOD_ID)
                           || entry.modifier().id().equals(Item.BASE_ATTACK_DAMAGE_ID)
                           || entry.modifier().id().equals(Item.BASE_ATTACK_SPEED_ID))
        ).toList());
        getData(itemStack).ifPresent(idMap -> {
            AttributeUtil.AttributeContext context = new AttributeUtil.AttributeContext();
            context.map = idMap;
            AttributeUtil.ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, itemStack);
            idMap = context.map;
            idMap.forEach((id, operationMap) -> {
                Attribute attribute = findAttribute(id);
                if (attribute != null) {
                    operationMap.forEach((attributeOperation, equipmentSlotMap) -> {
                        equipmentSlotMap.forEach((slot, operation) -> {
                            EquipmentSlotGroup slotGroup = EquipmentSlotProperty.getSlot(itemStack);
                            if (slot.left().isPresent()) {
                                slotGroup = slot.left().get();
                            }
                            if (slotGroup == null) {
                                slotGroup = EquipmentSlotGroup.ANY;
                            }
                            ResourceLocation slotId = AttributeUtil.getIDForSlot(slotGroup, attribute, attributeOperation);
                            double value = operation.getValue();
                            filteredList.add(new ItemAttributeModifiers.Entry(
                                    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute),
                                    new AttributeModifier(slotId, value, attributeOperation),
                                    slotGroup
                            ));
                        });
                    });
                }
            });
        });
        AttributeUtil.ItemVanillaAttributeContext context = new AttributeUtil.ItemVanillaAttributeContext();
        context.list = filteredList;
        AttributeUtil.VANILLA_ITEM_ATTRIBUTE_ADJUST.invoker().adjust(context, itemStack);
        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(context.list, true));
    }

    public Attribute findAttribute(ResourceLocation id) {
        var replacement = replaceMap.get(id.toString());
        if (replacement != null) {
            return replacement.get();
        }
        return BuiltInRegistries.ATTRIBUTE.get(id);
    }

    @Override
    public Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> merge(
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> left,
            Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> right,
            MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (id, leftMap, rightMap) -> {
            return MergeAble.mergeMap(leftMap, rightMap, mergeType, (operation, leftOperationMap, rightOperationMap) -> {
                return MergeAble.mergeMap(leftOperationMap, rightOperationMap, mergeType, (slot, leftJson, rightJson) -> {
                    return DoubleOperationResolvable.merge(leftJson, rightJson, mergeType);
                });
            });
        });
    }

    public Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> initialize(Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> map, ModuleInstance moduleInstance) {
        Map<ResourceLocation, Map<AttributeModifier.Operation, Map<Either<EquipmentSlotGroup, Boolean>, DoubleOperationResolvable>>> init = new LinkedHashMap<>();
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

    public static class AttributeJson {
        public static Codec<EquipmentSlotGroup> EQUIPMENTSLOT_CODEC = EquipmentSlotGroup.CODEC;

        public String attribute;
        public String value;
        public String operation;
        @CodecBehavior.Override("EQUIPMENTSLOT_CODEC")
        public EquipmentSlotGroup slot;
        @CodecBehavior.Optional
        @AutoCodec.Name("target_operation")
        public String targetOperation;
    }
}
