package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import io.netty.handler.codec.DecoderException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.ItemInventoryManager;
import smartin.miapi.modules.properties.util.InitializeAble;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface InventoryFeatureType<T>
        extends MergeAble<T>, InitializeAble<T> {

    ResourceLocation id();

    Codec<T> codec();

    default boolean allows(ItemStack stack, T featureData) {
        return true;
    }

    public final class FeatureSet implements MergeAble<FeatureSet>, InitializeAble<FeatureSet> {

        public static final Codec<InventoryFeatureType<?>> INVENTORY_FEATURE_CODEC =
                Miapi.ID_CODEC.xmap(
                        r -> {
                            InventoryFeatureType<?> p = ItemInventoryManager.INVENTORY_FEATURE_REGISTRY.get(r);
                            if (p == null) {
                                throw new RuntimeException(new DecoderException("Could not find inventory feature for key: " + r));
                            }
                            return p;
                        },
                        property -> {
                            var key = ItemInventoryManager.INVENTORY_FEATURE_REGISTRY.findKey(property);
                            if (key == null) {
                                throw new RuntimeException("Could not find registry key for inventory feature: " + property);
                            }
                            return key;
                        }
                );

        public static final Codec<FeatureSet> CODEC = Codec.dispatchedMap(
                INVENTORY_FEATURE_CODEC,
                feature -> {
                    if (feature == null) {
                        throw new IllegalArgumentException("InventoryFeature codec must not be null");
                    }
                    if (feature instanceof InventoryFeatureType<?> f) {
                        return (Codec<Object>) f.codec();
                    }
                    throw new IllegalArgumentException("InventoryFeature codec must not be null");
                }
        ).xmap(
                FeatureSet::new,
                set -> set.features
        );

        Map<InventoryFeatureType<?>, Object> features;

        public FeatureSet(Map<InventoryFeatureType<?>, Object> features) {
            this.features = Map.copyOf(features);
        }

        public Map<InventoryFeatureType<?>, Object> all() {
            return features;
        }

        public <T> boolean hasValue(InventoryFeatureType<T> feature, T value) {
            return Objects.equals(features.get(feature), value);
        }

        public <T> boolean matches(InventoryFeatureType<T> feature, Predicate<T> predicate) {
            return get(feature).filter(predicate).isPresent();
        }

        @SuppressWarnings("unchecked")
        public <T> void forEach(BiConsumer<InventoryFeatureType<T>, T> iterator) {
            features.forEach((feature, value) -> {
                iterator.accept((InventoryFeatureType<T>) feature, (T) value);
            });
        }

        @SuppressWarnings("unchecked")
        public <T> boolean has(InventoryFeatureType<T> feature) {
            return features.containsKey(feature);
        }

        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(InventoryFeatureType<T> feature) {
            Object value = features.get(feature);
            if (value != null) {
                return Optional.of((T) value);
            }
            return Optional.empty();
        }

        public FeatureSet merge(FeatureSet other, MergeType type) {
            Map<InventoryFeatureType<?>, Object> out = new HashMap<>(this.features);

            for (var e : other.features.entrySet()) {
                out.merge(
                        e.getKey(),
                        e.getValue(),
                        (a, b) -> mergeValue(e.getKey(), a, b, type)
                );
            }

            return new FeatureSet(out);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Object mergeValue(
                InventoryFeatureType feature,
                Object a,
                Object b,
                MergeType type
        ) {
            return feature.merge(a, b, type);
        }

        @Override
        public FeatureSet initialize(FeatureSet property, ModuleInstance context) {
            Map<InventoryFeatureType<?>, Object> initialized = new HashMap<>();
            forEach(((inventoryFeatureType, object) -> {
                initialized.put(inventoryFeatureType, inventoryFeatureType.initialize(object, context));
            }));
            return new FeatureSet(initialized);
        }

        @Override
        public FeatureSet merge(FeatureSet left, FeatureSet right, MergeType mergeType) {
            Map<InventoryFeatureType<?>, Object> out = new HashMap<>(left.features);

            for (var e : right.features.entrySet()) {
                out.merge(
                        e.getKey(),
                        e.getValue(),
                        (a, b) -> mergeValue(e.getKey(), a, b, mergeType)
                );
            }

            return new FeatureSet(out);
        }
    }
}