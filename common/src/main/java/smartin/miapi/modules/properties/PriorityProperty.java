package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.Optional;

/**
 * influences the ordering inside the gui
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
