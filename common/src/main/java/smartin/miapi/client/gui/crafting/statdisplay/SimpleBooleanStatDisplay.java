package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SimpleBooleanStatDisplay extends SingleStatDisplayBoolean {

    private final DoubleOperationResolvable resolvable;
    private final Function<ItemStack, Boolean> valueFunction;

    public SimpleBooleanStatDisplay(
            StatListWidget.TextGetter title,
            StatListWidget.TextGetter hover,
            DoubleOperationResolvable resolvable
    ) {
        super(0, 0, 51, 19, title, hover);
        this.resolvable = resolvable;
        this.valueFunction = null;
    }

    public SimpleBooleanStatDisplay(
            StatListWidget.TextGetter title,
            StatListWidget.TextGetter hover,
            Function<ItemStack, Boolean> valueFunction
    ) {
        super(0, 0, 51, 19, title, hover);
        this.resolvable = null;
        this.valueFunction = valueFunction;
    }

    @Override
    public boolean getValueItemStack(ItemStack itemStack) {
        if (valueFunction != null) {
            return valueFunction.apply(itemStack);
        }

        return resolvable != null && resolvable.getValue() != 0.0;
    }

    @Override
    public boolean hasValueItemStack(ItemStack itemStack) {
        if (valueFunction != null) {
            return true;
        }

        return resolvable != null;
    }

    public DoubleOperationResolvable getResolvable() {
        return resolvable;
    }

    public static Builder builder(DoubleOperationResolvable resolvable) {
        return new Builder(resolvable);
    }

    public static Builder builder(Function<ItemStack, Boolean> valueFunction) {
        return new Builder(valueFunction);
    }

    public static class Builder {
        private final DoubleOperationResolvable resolvable;
        private final Function<ItemStack, Boolean> valueFunction;

        public StatListWidget.TextGetter name;
        public StatListWidget.TextGetter hoverDescription = stack -> Component.empty();
        public String translationKey = "";

        private Builder(DoubleOperationResolvable resolvable) {
            this.resolvable = resolvable;
            this.valueFunction = null;
        }

        private Builder(Function<ItemStack, Boolean> valueFunction) {
            this.resolvable = null;
            this.valueFunction = valueFunction;
        }

        public Builder setName(Component name) {
            this.name = stack -> name;
            return this;
        }

        public Builder setName(StatListWidget.TextGetter name) {
            this.name = name;
            return this;
        }

        public Builder setTranslationKey(ResourceLocation key) {
            translationKey = Miapi.toLangString(key);

            name = stack -> Component.translatable(
                    Miapi.MOD_ID + ".stat." + translationKey,
                    getBooleanValue(stack)
            );

            hoverDescription = stack -> Component.translatable(
                    Miapi.MOD_ID + ".stat." + translationKey + ".description",
                    getBooleanValue(stack)
            );

            return this;
        }

        public Builder setHoverDescription(Component hoverDescription) {
            this.hoverDescription = stack -> hoverDescription;
            return this;
        }

        public Builder setHoverDescription(StatListWidget.TextGetter hoverDescription) {
            this.hoverDescription = hoverDescription;
            return this;
        }

        private Object getBooleanValue(ItemStack stack) {
            if (valueFunction != null) {
                return valueFunction.apply(stack);
            }

            return resolvable != null && resolvable.getValue() != 0.0;
        }

        public SimpleBooleanStatDisplay build() {
            if (name == null) {
                throw new IllegalStateException("Name is required");
            }

            if (resolvable == null && valueFunction == null) {
                throw new IllegalStateException("A resolvable or value function is required");
            }

            if (resolvable != null && valueFunction != null) {
                throw new IllegalStateException("Only one backing value may be specified");
            }

            if (resolvable != null) {
                return new SimpleBooleanStatDisplay(
                        name,
                        hoverDescription,
                        resolvable
                );
            }

            return new SimpleBooleanStatDisplay(
                    name,
                    hoverDescription,
                    valueFunction
            );
        }
    }
}