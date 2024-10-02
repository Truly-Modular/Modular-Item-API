package smartin.miapi.modules.properties.attributes;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.Holder;
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
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeType;

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
public class AttributeProperty extends CodecProperty<List<AttributeProperty.AttributeJson>> implements ComponentApplyProperty {
    public static final ResourceLocation KEY = Miapi.id("attributes");
    public static AttributeProperty property;
    public static final Map<String, Supplier<Attribute>> replaceMap = new HashMap<>();
    public static final Map<Attribute, Float> priorityMap = new HashMap<>();
    public static final List<AttributeTransformer> attributeTransformers = new ArrayList<>();
    public static Codec<List<AttributeJson>> CODEC = Codec.list(AutoCodec.of(AttributeJson.class).codec());

    public AttributeProperty() {
        super(CODEC);
        property = this;
        ModularItemCache.setSupplier(KEY.toString(), (AttributeProperty::createAttributeMap));
        priorityMap.put(Attributes.ARMOR.value(), -15.0f);
        priorityMap.put(Attributes.ARMOR_TOUGHNESS.value(), -14.0f);
        priorityMap.put(Attributes.KNOCKBACK_RESISTANCE.value(), -13.0f);
        priorityMap.put(Attributes.ATTACK_DAMAGE.value(), -12.0f);
        priorityMap.put(AttributeRegistry.MAGIC_DAMAGE.value(), -11.5f);
        priorityMap.put(Attributes.ATTACK_SPEED.value(), -11.0f);
        priorityMap.put(AttributeRegistry.CRITICAL_DAMAGE.value(), -10.9f);
        priorityMap.put(AttributeRegistry.CRITICAL_CHANCE.value(), -10.8f);
        priorityMap.put(AttributeRegistry.PROJECTILE_DAMAGE.value(), -10.0f);
        priorityMap.put(AttributeRegistry.PROJECTILE_CRIT_MULTIPLIER.value(), -9.5f);
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

    /**
     * return all attributemodifiers of an itemstack
     *
     * @param itemStack
     * @return
     */
    public static Multimap<Holder<Attribute>, EntityAttributeModifierHolder> getAttributeModifiers(ItemStack itemStack) {
        Multimap<Holder<Attribute>, EntityAttributeModifierHolder> map = getAttributeModifiersRaw(itemStack);
        Multimap<Holder<Attribute>, EntityAttributeModifierHolder> map2 = ArrayListMultimap.create();
        map.entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
            map2.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
        }));
        map = map2;
        for (AttributeTransformer transformer : attributeTransformers) {
            Multimap<Holder<Attribute>, EntityAttributeModifierHolder> map3 = ArrayListMultimap.create();
            transformer.transform(map, itemStack).entries().forEach((entityAttributeEntityAttributeModifierHolderEntry -> {
                map3.put(entityAttributeEntityAttributeModifierHolderEntry.getKey(), entityAttributeEntityAttributeModifierHolderEntry.getValue());
            }));
            map = map3;
        }
        return map;
    }

    /**
     * returns the raw modifiers, shouldnt be used widely
     *
     * @param itemStack
     * @return
     */
    public static Multimap<Holder<Attribute>, EntityAttributeModifierHolder> getAttributeModifiersRaw(ItemStack itemStack) {
        Multimap<Holder<Attribute>, EntityAttributeModifierHolder> multimap = ArrayListMultimap.create();
        return ModularItemCache.get(itemStack, KEY.toString(), multimap);
    }

    /**
     * Generates the multimap for the Cache
     *
     * @param itemStack
     * @return
     */
    private static Map<EquipmentSlotGroup, Multimap<Holder<Attribute>, AttributeModifier>> equipmentSlotMultimapMapGenerate(ItemStack itemStack) {
        Map<EquipmentSlotGroup, Multimap<Holder<Attribute>, AttributeModifier>> map = new HashMap<>();
        Map<Pair<EquipmentSlotGroup, Holder<Attribute>>, List<EntityAttributeModifierHolder>> equipentSlotMap = new HashMap<>();
        AttributeProperty.getAttributeModifiers(itemStack).forEach((attribute, info) -> {
            List<EntityAttributeModifierHolder> infos = equipentSlotMap.getOrDefault(new Pair<>(info.slot(), attribute), new ArrayList<>());
            infos.add(info);
            equipentSlotMap.put(new Pair<>(info.slot(), attribute), infos);
        });
        equipentSlotMap.forEach((pair, values) -> {
            List<AttributeModifier> addition = new ArrayList<>();
            List<AttributeModifier> multiplication = new ArrayList<>();
            List<AttributeModifier> multiplyTotal = new ArrayList<>();
            for (EntityAttributeModifierHolder holder : values) {
                switch (holder.mergeTo()) {
                    case ADD_VALUE -> addition.add(holder.attributeModifier());
                    case ADD_MULTIPLIED_BASE -> multiplication.add(holder.attributeModifier());
                    case ADD_MULTIPLIED_TOTAL -> multiplyTotal.add(holder.attributeModifier());
                }
            }
            var slotMap = map.getOrDefault(pair.getFirst(), ArrayListMultimap.create());
            mergeAddAttributes(pair, addition).ifPresent(addAttribute ->
                    slotMap.put(pair.getSecond(), addAttribute));
            mergeMultiplyBaseAttributes(pair, multiplication).ifPresent(addAttribute ->
                    slotMap.put(pair.getSecond(), addAttribute));
            mergeMultiplyTotalAttributes(pair, multiplyTotal).ifPresent(addAttribute ->
                    slotMap.put(pair.getSecond(), addAttribute));
            map.put(pair.getFirst(), slotMap);
        });

        Map<EquipmentSlotGroup, Multimap<Holder<Attribute>, AttributeModifier>> sortedMap = new HashMap<>();
        map.forEach((slot, attributes) -> {
            sortedMap.put(slot, AttributeUtil.sortMultimap(attributes));
        });
        return sortedMap;
    }

    private static Optional<AttributeModifier> mergeAddAttributes(Pair<EquipmentSlotGroup, Holder<Attribute>> pair, List<AttributeModifier> addition) {
        double baseValue = pair.getSecond().value().getDefaultValue();
        double startValue = baseValue;
        double multiply = 1;
        boolean hasValue = false;
        for (AttributeModifier entityAttributeModifier : addition) {
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                startValue += entityAttributeModifier.amount();
                hasValue = true;
            }
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                multiply += entityAttributeModifier.amount();
            }
        }
        startValue = startValue * multiply;
        for (AttributeModifier entityAttributeModifier : addition) {
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                startValue = startValue * (entityAttributeModifier.amount() + 1);
            }
        }
        startValue = startValue - baseValue;
        AttributeModifier entityAttributeModifier = null;
        if ((startValue != 0 || hasValue) && !Double.isNaN(startValue)) {
            ResourceLocation id = AttributeUtil.getIDForSlot(pair.getFirst(), pair.getSecond().value(), AttributeModifier.Operation.ADD_VALUE);
            entityAttributeModifier = new AttributeModifier(id, startValue, AttributeModifier.Operation.ADD_VALUE);
        }
        return Optional.ofNullable(entityAttributeModifier);
    }

    private static Optional<AttributeModifier> mergeMultiplyBaseAttributes(Pair<EquipmentSlotGroup, Holder<Attribute>> pair, List<AttributeModifier> multiplyList) {
        double multiply = 0;
        for (AttributeModifier entityAttributeModifier : multiplyList) {
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                multiply += entityAttributeModifier.amount();
            }
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Base(*)");
            }
        }
        for (AttributeModifier entityAttributeModifier : multiplyList) {
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                multiply = (multiply + 1) * (entityAttributeModifier.amount() + 1) - 1;
            }
        }
        AttributeModifier entityAttributeModifier = null;
        if (!Double.isNaN(multiply) && multiply != 1) {
            ResourceLocation id = AttributeUtil.getIDForSlot(pair.getFirst(), pair.getSecond().value(), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            entityAttributeModifier = new AttributeModifier(id, multiply, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
        return Optional.ofNullable(entityAttributeModifier);
    }

    private static Optional<AttributeModifier> mergeMultiplyTotalAttributes(Pair<EquipmentSlotGroup, Holder<Attribute>> pair, List<AttributeModifier> multiplyList) {
        double multiply = 1;
        for (AttributeModifier entityAttributeModifier : multiplyList) {
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                multiply = multiply * entityAttributeModifier.amount();
            }
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                Miapi.LOGGER.warn("Operation Addition(+) is not supported to be merged to Multiply Total(**)");
            }
            if (entityAttributeModifier.operation().equals(AttributeModifier.Operation.ADD_VALUE)) {
                Miapi.LOGGER.warn("Operation Multiply Base(*) is not supported to be merged to Multiply Total(**)");
            }
        }
        AttributeModifier entityAttributeModifier = null;
        if (!Double.isNaN(multiply) && multiply != 1) {
            ResourceLocation id = AttributeUtil.getIDForSlot(pair.getFirst(), pair.getSecond().value(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            entityAttributeModifier = new AttributeModifier(id, multiply, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
        return Optional.ofNullable(entityAttributeModifier);
    }

    public static Multimap<Holder<Attribute>, EntityAttributeModifierHolder> createAttributeMap(ItemStack itemStack) {
        ModuleInstance rootInstance = ItemModule.getModules(itemStack);
        Multimap<Holder<Attribute>, EntityAttributeModifierHolder> attributeModifiers = ArrayListMultimap.create();
        for (ModuleInstance instance : rootInstance.allSubModules()) {
            getAttributeModifiers(instance, attributeModifiers);
        }
        return attributeModifiers;
    }

    public static void getAttributeModifiers(ModuleInstance instance, Multimap<Holder<Attribute>, EntityAttributeModifierHolder> attributeModifiers) {
        property.getData(instance).ifPresent(attributeJsons -> attributeJsons.forEach(attributeJson -> {

            AttributeModifier.Operation operation = getOperation(attributeJson.operation);
            AttributeModifier.Operation baseTarget = getOperation(attributeJson.targetOperation);
            Attribute attribute = replaceMap.getOrDefault(attributeJson.attribute, () -> BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeJson.attribute))).get();
            if (attribute == null) {
                Miapi.LOGGER.warn(String.valueOf(BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.parse(attributeJson.attribute))));
                Miapi.LOGGER.warn("Attribute is null " + attributeJson.attribute + " on module " + instance.module.id() + " this should not have happened.");
            } else {
                Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute);
                ResourceLocation id = AttributeUtil.getIDForSlot(attributeJson.slot, attribute, operation);
                attributeModifiers.put(attributeHolder, new EntityAttributeModifierHolder(new AttributeModifier(id, attributeJson.evaluatedValue, operation), attributeJson.slot, baseTarget));
            }
        }));
    }

    private static AttributeModifier.Operation getOperation(String operationString) {
        if (operationString == null) {
            return AttributeModifier.Operation.ADD_VALUE;
        }
        return switch (operationString) {
            case "*" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "**" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    @Override
    public List<AttributeJson> merge(List<AttributeJson> left, List<AttributeJson> right, MergeType mergeType) {
        List<AttributeJson> merged = new ArrayList<>(left);
        merged.addAll(right);
        return merged;
    }

    public List<AttributeJson> initialize(List<AttributeJson> property, ModuleInstance context) {
        property.forEach(entry -> entry.evaluatedValue = entry.value.evaluate(context));
        return property;
    }

    @Override
    public void updateComponent(ItemStack itemStack, RegistryAccess registryAccess) {
        var attributes = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> filteredList = new ArrayList<>(attributes.modifiers().stream().filter(
                entry -> !(entry.modifier().id().getNamespace().equals(Miapi.MOD_ID)
                           || entry.modifier().id().equals(Item.BASE_ATTACK_DAMAGE_ID)
                           || entry.modifier().id().equals(Item.BASE_ATTACK_SPEED_ID))
        ).toList());
        equipmentSlotMultimapMapGenerate(itemStack).forEach(((group, attributeAttributeModifierMultimap) -> {
            attributeAttributeModifierMultimap.forEach((attribute, attributeModifier) -> {
                filteredList.add(new ItemAttributeModifiers.Entry(attribute, attributeModifier, group));
            });
        }));
        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(filteredList, true));
    }

    public record EntityAttributeModifierHolder(AttributeModifier attributeModifier, EquipmentSlotGroup slot,
                                                AttributeModifier.Operation mergeTo) {
    }

    public interface AttributeTransformer {
        Multimap<Holder<Attribute>, EntityAttributeModifierHolder> transform(Multimap<Holder<Attribute>, EntityAttributeModifierHolder> map, ItemStack itemstack);
    }

    public static class AttributeJson {
        public static Codec<EquipmentSlotGroup> EQUIPMENTSLOT_CODEC = EquipmentSlotGroup.CODEC;

        public String attribute;
        public StatResolver.DoubleFromStat value = new StatResolver.DoubleFromStat(0);
        public String operation;
        @CodecBehavior.Override("EQUIPMENTSLOT_CODEC")
        public EquipmentSlotGroup slot;
        @CodecBehavior.Optional
        @AutoCodec.Name("target_operation")
        public String targetOperation;
        @AutoCodec.Ignored
        public double evaluatedValue;
    }
}
