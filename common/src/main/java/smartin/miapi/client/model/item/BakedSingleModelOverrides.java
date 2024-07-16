package smartin.miapi.client.model.item;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.DynamicBakery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class BakedSingleModelOverrides extends ItemOverrides {
    public final DynamicBakedOverride[] dynamicOverrides;
    public final ResourceLocation[] dynamicConditionTypes;

    public BakedSingleModelOverrides(Map<ConditionHolder, BakedModel> modelHashMap) {
        super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
        this.dynamicConditionTypes = modelHashMap.keySet().stream().flatMap(conditionHolder -> conditionHolder.conditions.stream()).map(Condition::getType).distinct().toArray(ResourceLocation[]::new);
        Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();

        for (int i = 0; i < this.dynamicConditionTypes.length; ++i) {
            object2IntMap.put(this.dynamicConditionTypes[i], i);
        }

        List<DynamicBakedOverride> list = Lists.newArrayList();
        modelHashMap.forEach(((conditionHolder, bakedModel) -> {
            InlinedCondition[] inlinedConditions = conditionHolder.conditions.stream().map((condition) -> {
                int i = object2IntMap.getInt(condition.getType());
                return new InlinedCondition(i, condition.getThreshold());
            }).toArray(InlinedCondition[]::new);
            list.add(new DynamicBakedOverride(inlinedConditions, bakedModel));
        }));
        DynamicBakedOverride[] dynamicCondition = new DynamicBakedOverride[list.size()];
        for (int i = 0; i < list.size(); i++) {
            dynamicCondition[i] = list.get(i);
        }
        this.dynamicOverrides = dynamicCondition;
    }


    @Override
    public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        if (this.dynamicOverrides.length != 0) {
            int i = this.dynamicConditionTypes.length;
            float[] fs = new float[i];

            for (int j = 0; j < i; ++j) {
                ResourceLocation identifier = this.dynamicConditionTypes[j];
                ItemPropertyFunction modelPredicateProvider = ItemProperties.getProperty(stack, identifier);
                if (modelPredicateProvider != null) {
                    fs[j] = modelPredicateProvider.call(stack, world, entity, seed);
                } else {
                    fs[j] = Float.NEGATIVE_INFINITY;
                }
            }
            for (int j = 0; j < this.dynamicOverrides.length; ++j) {
                //Miapi.LOGGER.warn(String.valueOf(j));
                DynamicBakedOverride bakedOverride = this.dynamicOverrides[j];
                if (bakedOverride.test(fs)) {
                    if (bakedOverride.model != null) {
                        return bakedOverride.model;
                    }
                }
            }
        }
        return model;
    }

    @Environment(EnvType.CLIENT)
    static class DynamicBakedOverride {
        private final InlinedCondition[] conditions;
        @Nullable
        final BakedModel model;

        DynamicBakedOverride(InlinedCondition[] conditions, @Nullable BakedModel model) {
            this.conditions = conditions;
            this.model = model;
        }

        boolean test(float[] values) {

            for (InlinedCondition inlinedCondition : this.conditions) {
                float f = values[inlinedCondition.index];
                if (f < inlinedCondition.threshold) {
                    return false;
                }
            }

            return true;
        }
    }

    @Environment(EnvType.CLIENT)
    private record InlinedCondition(int index, float threshold) {
    }

    @Environment(value = EnvType.CLIENT)
    public static class Condition {
        private final ResourceLocation type;
        private final float threshold;

        public Condition(ItemOverride.Predicate condition) {
            this.type = condition.getProperty();
            this.threshold = condition.getValue();
        }

        public Condition(ResourceLocation type, float threshold) {
            this.type = type;
            this.threshold = threshold;
        }

        public ResourceLocation getType() {
            return this.type;
        }

        public float getThreshold() {
            return this.threshold;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof Condition otherCondition) {
                if (otherCondition.type.equals(this.type) && otherCondition.threshold == this.threshold) {
                    return true;
                }
            }
            if (obj instanceof ItemOverride.Predicate otherCondition) {
                return otherCondition.getProperty().equals(this.type) && otherCondition.getValue() == this.threshold;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Condition - " + type.toString() + " - " + threshold;
        }
    }

    public static class ConditionHolder {
        public ResourceLocation identifier;
        public List<Condition> conditions;

        public ConditionHolder(ItemOverride override) {
            identifier = override.getModel();
            conditions = override.getPredicates().map(condition -> new Condition(condition)).toList();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj instanceof ConditionHolder otherCondition) {
                if (conditions.size() == otherCondition.conditions.size()) {
                    for (Condition condition : conditions) {
                        if (!otherCondition.conditions.contains(condition)) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isAcceptable(ConditionHolder other) {
            for (Condition condition : other.conditions) {
                Optional<Condition> exist = conditions.stream().filter(condition1 -> condition1.type.equals(condition.type)).findFirst();
                if (exist.isPresent()) {
                    if (condition.threshold < exist.get().threshold) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + conditions.size();
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConditionHolder-" + identifier.toString() + "\n");
            for (Condition condition : conditions) {
                builder.append(condition.toString() + ",");
            }
            return builder.toString();
        }
    }
}
