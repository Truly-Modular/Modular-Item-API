package smartin.miapi.modules.properties.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.ModuleInstance;

import java.util.Optional;
/**
 * @header Boolean Resolvable
 * @description_start
 * This is in concept just like a Double resolvable.
 * This is regarded true if the outcome is > 0.
 * false is equivalent to 0, writing true in the json is the same as writing 1
 * @desciption_end
 * @path /data_types/boolean_resolvable
 * @keywords Boolean Resolvable,BooleanResolvable
 * @data a string or number representing a calculation/numeric/boolean value
 */
public abstract class ComplexBooleanProperty extends DoubleProperty {
    boolean defaultValue;

    protected ComplexBooleanProperty(ResourceLocation key, boolean defaultValue) {
        super(key);
        this.defaultValue = defaultValue;
    }

    public boolean isTrue(ItemStack itemStack) {
        Double value = getValue(itemStack).orElse(null);
        return value != null ? value > 0 : defaultValue;
    }

    public boolean isTrue(ModuleInstance moduleInstance) {
        Optional<DoubleOperationResolvable> optional = getData(moduleInstance);
        return optional.map(doubleOperationResolvable -> doubleOperationResolvable.evaluate(0.0, 0.0) > 0).orElseGet(() -> defaultValue);
    }

    public boolean hasValue(ItemStack itemStack) {
        return isTrue(itemStack) != defaultValue;
    }
}
