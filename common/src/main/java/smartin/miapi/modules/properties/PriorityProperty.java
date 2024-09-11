package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Optional;

/**
 * @header UI Priority Property
 * @path /data_types/properties/priority
 * @description_start
 * The PriorityProperty affects the ordering of items or modules within the GUI. This property assigns a priority value
 * to each item or module, which influences its placement or sorting in graphical user interfaces where multiple items
 * or modules are displayed. A lower value will be placed first in the list and higher values at the end.
 *
 * @description_end
 * @data priority: the priority for sorting
 */

public class PriorityProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("priority");
    public static PriorityProperty property;


    public PriorityProperty() {
        super(KEY);
        property = this;
    }

    public static double getFor(ItemModule module) {
        Optional<DoubleOperationResolvable> resolvable = property.getData(new ModuleInstance(module));
        if (resolvable.isPresent()) {
            return resolvable.get().getValue();
        }
        return 0.0;
    }
}
