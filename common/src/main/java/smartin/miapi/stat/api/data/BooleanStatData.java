package smartin.miapi.stat.api.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.stat.StatTypes;
import smartin.miapi.stat.api.Stat;
import smartin.miapi.stat.api.StatGroup;
import smartin.miapi.stat.api.StatMetaData;
import smartin.miapi.stat.api.StatText;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.function.Function;

public record BooleanStatData(
        Function<ItemStack, Boolean> getter,
        Function<ItemStack, DoubleOperationResolvable> resolvableGetter,
        boolean defaultValue,
        double priority
) implements StatMetaData<Boolean> {

    @Override
    public Boolean getData(ItemStack stack) {
        return getter.apply(stack);
    }

    public double getPriority() {
        return priority;
    }

    public boolean hasStat(ItemStack stack) {
        return !(getData(stack) == null || getData(stack) == defaultValue);
    }

    /**
     * Gets the underlying resolvable value.
     * <p>
     * This is useful for ComplexBooleanProperty instances where the
     * boolean result is derived from a DoubleOperationResolvable.
     */
    public DoubleOperationResolvable getResolvable(ItemStack stack) {
        return resolvableGetter.apply(stack);
    }

    public static BooleanStatBuilder forProperty(
            ResourceLocation id,
            StatGroup group,
            ComplexBooleanProperty property
    ) {
        return new BooleanStatBuilder(id, group, property);
    }

    public static BooleanStatBuilder getBuilder(
            ResourceLocation id,
            StatText<Boolean, BooleanStatData> name,
            StatText<Boolean, BooleanStatData> description,
            StatGroup group,
            Function<ItemStack, Boolean> getter
    ) {
        return new BooleanStatBuilder(
                id,
                name,
                description,
                group,
                getter,
                stack -> null
        );
    }

    public static BooleanStatBuilder getBuilder(
            ResourceLocation id,
            StatText<Boolean, BooleanStatData> name,
            StatText<Boolean, BooleanStatData> description,
            StatGroup group,
            Function<ItemStack, Boolean> getter,
            Function<ItemStack, DoubleOperationResolvable> resolvableGetter
    ) {
        return new BooleanStatBuilder(
                id,
                name,
                description,
                group,
                getter,
                resolvableGetter
        );
    }

    public static class BooleanStatBuilder {

        private final ResourceLocation id;
        private StatText<Boolean, BooleanStatData> name;
        private StatText<Boolean, BooleanStatData> description;
        private final StatGroup group;
        private final Function<ItemStack, Boolean> getter;
        private final Function<ItemStack, DoubleOperationResolvable> resolvableGetter;
        private double priority = 0;

        private boolean defaultValue = false;

        private BooleanStatBuilder(
                ResourceLocation id,
                StatGroup group,
                ComplexBooleanProperty property
        ) {
            this.id = id;

            String translationKey = Miapi.toLangString(id);

            this.name = (stack, data, meta) ->
                    Component.translatable(
                            Miapi.MOD_ID + ".stat." + translationKey,
                            meta.getData(stack)
                    );

            this.description = (stack, data, meta) ->
                    Component.translatable(
                            Miapi.MOD_ID + ".stat." + translationKey + ".description",
                            meta.getData(stack)
                    );

            this.group = group;

            this.getter = property::isTrue;
            this.resolvableGetter = stack ->
                    property.getData(stack).orElse(null);

            this.defaultValue = false;
        }

        private BooleanStatBuilder(
                ResourceLocation id,
                StatText<Boolean, BooleanStatData> name,
                StatText<Boolean, BooleanStatData> description,
                StatGroup group,
                Function<ItemStack, Boolean> getter,
                Function<ItemStack, DoubleOperationResolvable> resolvableGetter
        ) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.group = group;
            this.getter = getter;
            this.resolvableGetter = resolvableGetter;
        }

        public BooleanStatBuilder setName(
                StatText<Boolean, BooleanStatData> name
        ) {
            this.name = name;
            return this;
        }

        public BooleanStatBuilder setDescription(
                StatText<Boolean, BooleanStatData> description
        ) {
            this.description = description;
            return this;
        }

        public BooleanStatBuilder setDefaultValue(boolean defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }


        /**
         * Sets the priority used when rendering this stat. Lower values are rendered earlier.
         */
        public BooleanStatBuilder setPriority(double priority) {
            this.priority = priority;
            return this;
        }

        public Stat<Boolean, BooleanStatData> register() {
            var metaData = new BooleanStatData(
                    getter,
                    resolvableGetter,
                    defaultValue,
                    priority
            );

            Stat<Boolean, BooleanStatData> stat = new Stat<>(
                    id.withPrefix("boolean." + group.getID().toLanguageKey() + "."),
                    name,
                    description,
                    StatTypes.BOOLEAN_TYPE,
                    group,
                    metaData
            );
            Stat.STAT_REGISTRY.register(id, stat);
            return stat;
        }
    }
}