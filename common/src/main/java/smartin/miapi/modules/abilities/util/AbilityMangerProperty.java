package smartin.miapi.modules.abilities.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This property manages the active {@link ItemUseAbility}
 */
public class AbilityMangerProperty extends CodecProperty<Map<ItemUseAbility<?>, Object>> {
    public static String KEY = "ability_context";
    public static AbilityMangerProperty property;
    public static Codec<Map<ItemUseAbility<?>, Object>> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(Map<ItemUseAbility<?>, Object> input, DynamicOps<T> ops, T prefix) {
            //TODO:fixme
            return DataResult.error(() -> "properties are not meant to be decoded");
        }

        @Override
        public <T> DataResult<Pair<Map<ItemUseAbility<?>, Object>, T>> decode(DynamicOps<T> ops, T input) {
            Map<ItemUseAbility<?>, Object> abilityMap = new LinkedHashMap<>();
            ops.getMap(input).getOrThrow().entries();
            ops.getMapValues(input).getOrThrow().toList().forEach((pair) -> {
                String resourceLocation = Codec.STRING.decode(ops, pair.getFirst()).getOrThrow().getFirst();
                ItemUseAbility<?> itemUseAbility = ItemAbilityManager.useAbilityRegistry.get(resourceLocation);
                if (itemUseAbility == null) {
                    Miapi.LOGGER.error("can not find ItemUseAbility " + resourceLocation);
                } else {
                    Object data = itemUseAbility.decode(ops, pair.getSecond());
                    abilityMap.put(itemUseAbility, data);
                }
            });
            return DataResult.success(new Pair<>(abilityMap, input));
        }
    };

    public AbilityMangerProperty() {
        super(CODEC);
        property = this;
    }

    public static boolean isPrimaryAbility(ItemUseAbility<?> itemUseAbility, ItemStack itemStack) {
        LinkedHashMap<ItemUseAbility<?>, Object> map = (LinkedHashMap<ItemUseAbility<?>, Object>) property.getData(itemStack).orElse(new LinkedHashMap<>());
        if (!map.sequencedEntrySet().isEmpty()) {
            return itemUseAbility == map.sequencedEntrySet().getFirst().getKey();
        }
        return false;
    }

    @Override
    public Map<ItemUseAbility<?>, Object> merge(Map<ItemUseAbility<?>, Object> left, Map<ItemUseAbility<?>, Object> right, MergeType mergeType) {
        Map<ItemUseAbility<?>, Object> merged = new LinkedHashMap<>(left);
        right.forEach(((itemUseAbility, o) -> {
            if (merged.containsKey(itemUseAbility)) {
                merged.put(itemUseAbility, mergeValues(itemUseAbility, merged.get(itemUseAbility), o, mergeType));
            } else {
                merged.put(itemUseAbility, o);
            }
        }));
        return merged;
    }

    public Map<ItemUseAbility<?>, Object> initialize(Map<ItemUseAbility<?>, Object> property, ModuleInstance context) {
        Map<ItemUseAbility<?>, Object> initialized = new LinkedHashMap<>();
        property.forEach((itemUseAbility, o) -> {
            initialized.put(itemUseAbility, initialize(itemUseAbility, o, context));
        });
        return initialized;
    }

    @SuppressWarnings("unchecked")
    private <T> T initialize(ItemUseAbility<T> itemUseAbility, Object value, ModuleInstance moduleInstance) {
        return (T) itemUseAbility.initialize((T) value, moduleInstance);
    }

    @SuppressWarnings("unchecked")
    private <T> T mergeValues(ItemUseAbility<T> itemUseAbility, Object leftValue, Object rightValue, MergeType mergeType) {
        return itemUseAbility.merge((T) leftValue, (T) rightValue, mergeType);
    }
}
