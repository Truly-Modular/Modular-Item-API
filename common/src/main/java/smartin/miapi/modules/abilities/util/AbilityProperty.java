package smartin.miapi.modules.abilities.util;


import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;
import java.util.stream.Stream;

public class AbilityProperty extends CodecProperty<Map<ResourceLocation, AbilityProperty.AbilityHolder>> {

    public AbilityProperty() {
        super(Codec.unboundedMap(ResourceLocation.CODEC, AbilityHolder.CODEC));
    }

    @Override
    public Map<ResourceLocation, AbilityHolder> merge(Map<ResourceLocation, AbilityHolder> left, Map<ResourceLocation, AbilityHolder> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType, (id, l, r) -> l.merge(l, r, mergeType));
    }

    public static class AbilityHolder implements MergeAble<AbilityHolder>, InitializeAble<AbilityHolder> {
        public static Codec<AbilityHolder> CODEC =
                ResourceLocation.CODEC.dispatch(AbilityHolder::getID,
                        AbilityHolder::createCodec
                );
        public final ItemUseAbility<Object> ability;
        public final Object data;

        public AbilityHolder(ItemUseAbility<Object> ability, Object data) {
            this.ability = ability;
            this.data = data;
        }

        @Override
        public AbilityHolder initialize(AbilityHolder property, ModuleInstance context) {
            Object initialized = ability.initialize(property.data, context);
            return new AbilityHolder(ability, initialized);
        }

        public ResourceLocation getID() {
            return RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.findKey(ability);
        }

        @Override
        public AbilityHolder merge(AbilityHolder left, AbilityHolder right, MergeType mergeType) {
            Object merged = ability.merge(left.data, right.data, mergeType);
            return new AbilityHolder(ability, merged);
        }

        public static MapCodec<AbilityHolder> createCodec(ResourceLocation id) {
            ItemUseAbility ability = RegistryInventory.ITEM_USE_ABILITY_MIAPI_REGISTRY.get(id);
            if (ability != null) {
                return new MapCodec<>() {
                    @Override
                    public <T> RecordBuilder<T> encode(AbilityHolder input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                        return null;
                    }

                    @Override
                    public <T> DataResult<AbilityHolder> decode(DynamicOps<T> ops, MapLike<T> input) {
                        return DataResult.error(() -> "Ability with id " + id + " was not found!");
                    }

                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> ops) {
                        return Stream.empty();
                    }
                };
            }
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            ability.getCodec().fieldOf("data").forGetter(holder -> ability.castTo(holder))
                    ).apply(instance, (data) -> new AbilityHolder(ability, data))
            );
        }
    }
}
