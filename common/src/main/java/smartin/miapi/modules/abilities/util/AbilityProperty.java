package smartin.miapi.modules.abilities.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.*;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class AbilityProperty extends CodecProperty<List<AbilityProperty.AbilityContext<?>>> {
    public static final String KEY = "ability_context";
    public static AbilityProperty property;

    public static Codec<Map<ItemUseAbility<?>, Object>> OLD_CODEC_RAW = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(Map<ItemUseAbility<?>, Object> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> encodedMap = new LinkedHashMap<>();
            RecordBuilder<T> map = ops.mapBuilder();

            for (Map.Entry<ItemUseAbility<?>, Object> entry : input.entrySet()) {
                ItemUseAbility<?> ability = entry.getKey();
                Object data = entry.getValue();

                // Retrieve the ability's registry key
                String abilityId = RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(ability).toString();
                if (abilityId == null) {
                    Miapi.LOGGER.error("Failed to encode ItemUseAbility: Ability not found in registry.");
                    continue;
                }

                // Encode ability ID
                DataResult<T> keyResult = Codec.STRING.encode(abilityId, ops, ops.empty());
                if (keyResult.error().isPresent()) {
                    Miapi.LOGGER.error("Failed to encode ItemUseAbility key: " + keyResult.error().get().message());
                    continue;
                }
                if (keyResult.result().isEmpty()) {
                    Miapi.LOGGER.error("Failed to encode ItemUseAbility key: " + abilityId);
                    continue;
                }

                // Encode ability data)
                DataResult<T> valueResult = DataResult.success(ability.encodeObject(ops, data));
                if (valueResult.error().isPresent()) {
                    Miapi.LOGGER.error("Failed to encode data for ability: " + abilityId + " - " + valueResult.error().get().message());
                    continue;
                }

                encodedMap.put(keyResult.result().get(), valueResult.result().get());
                map.add(keyResult.result().get(), valueResult.result().get());

            }
            return map.build(prefix);
        }


        @Override
        public <T> DataResult<Pair<Map<ItemUseAbility<?>, Object>, T>> decode(DynamicOps<T> ops, T input) {
            Map<ItemUseAbility<?>, Object> abilityMap = new LinkedHashMap<>();
            ops.getMap(input).getOrThrow().entries();
            ops.getMapValues(input).getOrThrow().toList().forEach((pair) -> {
                String resourceLocation = Codec.STRING.decode(ops, pair.getFirst()).getOrThrow().getFirst();
                ItemUseAbility<?> itemUseAbility = RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.get(resourceLocation);
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

    public static Codec<List<AbilityProperty.AbilityContext<?>>> OLD_CODEC = OLD_CODEC_RAW.xmap((a) -> {
        List<AbilityProperty.AbilityContext<?>> list = new ArrayList<>();
        a.forEach(((itemUseAbility, object) -> {
            list.add(new AbilityContext<>(Miapi.id("old_ability_system"), 0,
                    itemUseAbility,
                    new DoubleOperationResolvable(1),
                    new DoubleOperationResolvable(1),
                    new DoubleOperationResolvable(1), object));
        }));
        return list;
    }, b -> null);

    public static Codec<List<AbilityProperty.AbilityContext<?>>> CODEC = Codec.withAlternative(AbilityContext.CODEC.listOf(), OLD_CODEC);

    public static boolean isPrimaryAbility(ItemUseAbility<?> itemUseAbility, ItemStack itemStack) {
        if (VisualModularItem.isVisualModularItem(itemStack) && !ModularItem.isModularItem(itemStack)) {
            return false;
        }
        List<AbilityProperty.AbilityContext<?>> list = property.getData(itemStack).orElse(List.of());
        if (!list.isEmpty()) {
            return list.getFirst().ability.equals(itemUseAbility);
        }
        return false;
    }

    public AbilityProperty() {
        super(CODEC);
        property = this;
        RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.addCallback((id, ability) -> {
            if (id != null && ability != null) {
                if (ability.getCodec() == null) {
                    throw new RuntimeException("Ability has no Codec! " + id + " " + ability.toString());
                }
                AbilityContext.registerAbilityCodec(id, ability);
            }
        });
    }

    @Override
    public List<AbilityContext<?>> merge(List<AbilityContext<?>> left, List<AbilityContext<?>> right, MergeType mergeType) {
        Map<String, AbilityContext<?>> merged = new HashMap<>();
        Function<AbilityContext<?>, String> makeKey =
                h -> h.id + "|" + h.abilityId();

        for (AbilityContext<?> l : left) {
            merged.put(makeKey.apply(l), l);
        }

        for (AbilityContext<?> r : right) {
            merged.merge(
                    makeKey.apply(r),
                    r,
                    (l, rr) -> l.mergeUnchecked(l, rr, mergeType)
            );
        }

        return new ArrayList<>(merged.values());
    }


    public static class AbilityContext<T> implements MergeAble<AbilityContext<T>>, InitializeAble<AbilityContext<T>> {
        public static final Map<ResourceLocation, MapCodec<? extends AbilityContext<?>>> ABILITY_CODECS = new ConcurrentHashMap<>();
        public static final Codec<AbilityContext<?>> CODEC =
                ResourceLocation.CODEC.dispatch(
                        AbilityContext::abilityId,
                        AbilityContext::getCodecForType
                );

        public final float priority;
        public final ResourceLocation id;
        public final ItemUseAbility<T> ability;
        public final T data;
        public final DoubleOperationResolvable allowedOnBlock;
        public final DoubleOperationResolvable allowedOnEntity;
        public final DoubleOperationResolvable allowedOnAir;

        @SuppressWarnings("unchecked")
        public AbilityContext(ResourceLocation id, float priority, ItemUseAbility<T> ability, DoubleOperationResolvable allowedOnEntity, DoubleOperationResolvable allowedOnBlock, DoubleOperationResolvable allowedOnAir, Object data) {
            this.id = id;
            this.priority = priority;
            this.ability = ability;
            this.data = (T) data;
            this.allowedOnBlock = allowedOnBlock;
            this.allowedOnEntity = allowedOnEntity;
            this.allowedOnAir = allowedOnAir;
        }

        public AbilityContext(ResourceLocation generatedMaterialAbility, float priority, ItemUseAbility<T> ability, Object itemContext) {
            this(generatedMaterialAbility, priority, ability, new DoubleOperationResolvable(1), new DoubleOperationResolvable(1), new DoubleOperationResolvable(1), itemContext);
        }

        public ResourceLocation abilityId() {
            return RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(ability);
        }

        /* ---------------- Dispatch helpers ---------------- */

        private static MapCodec<? extends AbilityContext<?>> getCodecForType(ResourceLocation type) {
            MapCodec<? extends AbilityContext<?>> codec = ABILITY_CODECS.get(type);
            if (codec == null) {
                Miapi.LOGGER.error("No codec registered for ability type {}", type);
                return MapCodec.unit(new AbilityContext<>(type, 0, null, null)); // fallback dummy
            }
            return codec;
        }

        @SuppressWarnings("unchecked")
        public static <T> void registerAbilityCodec(ResourceLocation type, ItemUseAbility<T> ability) {
            MapCodec<AbilityContext<T>> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("id")
                            .forGetter(holder -> holder.id),
                    Codec.FLOAT.optionalFieldOf("priority", 0.0f)
                            .forGetter(holder -> holder.priority),
                    DoubleOperationResolvable.CODEC.optionalFieldOf("allowed_on_entity", new DoubleOperationResolvable(1))
                            .forGetter(holder -> holder.allowedOnEntity),
                    DoubleOperationResolvable.CODEC.optionalFieldOf("allowed_on_block", new DoubleOperationResolvable(1))
                            .forGetter(holder -> holder.allowedOnBlock),
                    DoubleOperationResolvable.CODEC.optionalFieldOf("allowed_on_air", new DoubleOperationResolvable(1))
                            .forGetter(holder -> holder.allowedOnAir),
                    ability.getCodec().fieldOf("data")
                            .forGetter(holder -> holder.data)
            ).apply(instance, (id, priority, allowedOnEntity, allowedOnBlock, allowedOnAir, data) -> new AbilityContext<>(id, priority, ability, allowedOnEntity, allowedOnBlock, allowedOnAir, data)));

            ABILITY_CODECS.put(type, codec);
            Miapi.LOGGER.debug("Registered codec for ability type {}", type);
        }

        /* ---------------- Lifecycle ---------------- */

        @Override
        public AbilityContext<T> initialize(AbilityContext<T> property, ModuleInstance context) {
            T initialized = ability.initialize(property.data, context);
            return new AbilityContext<>(id, priority, ability, property.allowedOnEntity.initialize(context), allowedOnBlock.initialize(context), allowedOnAir.initialize(context), initialized);
        }

        @SuppressWarnings("unchecked")
        public AbilityContext<?> mergeUnchecked(AbilityContext<?> left, AbilityContext<?> right, MergeType mergeType) {
            return merge((AbilityContext<T>) left, (AbilityContext<T>) right, mergeType);
        }

        @Override
        public AbilityContext<T> merge(AbilityContext<T> left, AbilityContext<T> right, MergeType mergeType) {
            T merged = ability.merge(left.data, right.data, mergeType);
            return new AbilityContext<>(id, Math.max(left.priority, right.priority),
                    ability,
                    DoubleOperationResolvable.merge(left.allowedOnEntity, right.allowedOnEntity, mergeType),
                    DoubleOperationResolvable.merge(left.allowedOnBlock, right.allowedOnBlock, mergeType),
                    DoubleOperationResolvable.merge(left.allowedOnAir, right.allowedOnAir, mergeType),
                    merged);
        }

        @Override
        public String toString() {
            return "AbilityHolder{" +
                   "id=" + id +
                   ", type=" + abilityId() +
                   ", priority=" + priority +
                   ", data=" + data +
                   '}';
        }
    }


}
