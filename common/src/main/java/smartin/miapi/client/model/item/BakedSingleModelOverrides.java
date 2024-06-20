package smartin.miapi.client.model.item;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.client.model.DynamicBakery;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BakedSingleModelOverrides extends ModelOverrideList {
    public final DynamicBakedOverride[] dynamicOverrides;
    public final Identifier[] dynamicConditionTypes;

    public BakedSingleModelOverrides(Map<ConditionHolder, BakedModel> modelHashMap) {
        super(DynamicBakery.dynamicBaker, null, new ArrayList<>());
        this.dynamicConditionTypes = modelHashMap.keySet().stream().flatMap(conditionHolder -> conditionHolder.conditions.stream()).map(Condition::getType).distinct().toArray(Identifier[]::new);
        Object2IntMap<Identifier> object2IntMap = new Object2IntOpenHashMap<>();

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
    public BakedModel apply(BakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        if (this.dynamicOverrides.length != 0) {
            Item item = stack.getItem();
            int i = this.dynamicConditionTypes.length;
            float[] fs = new float[i];

            for (int j = 0; j < i; ++j) {
                Identifier identifier = this.dynamicConditionTypes[j];
                ModelPredicateProvider modelPredicateProvider = ModelPredicateProviderRegistry.get(item, identifier);
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
        private final Identifier type;
        private final float threshold;

        public Condition(ModelOverride.Condition condition) {
            this.type = condition.getType();
            this.threshold = condition.getThreshold();
        }

        public Condition(Identifier type, float threshold) {
            this.type = type;
            this.threshold = threshold;
        }

        public Identifier getType() {
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
            if (obj instanceof ModelOverride.Condition otherCondition) {
                if (otherCondition.getType().equals(this.type) && otherCondition.getThreshold() == this.threshold) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Condition - " + type.toString() + " - " + threshold;
        }
    }

    public static class ConditionHolder {
        public Identifier identifier;
        public List<Condition> conditions;

        public ConditionHolder(ModelOverride override) {
            identifier = override.getModelId();
            conditions = override.streamConditions().map(condition -> new Condition(condition)).toList();
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
