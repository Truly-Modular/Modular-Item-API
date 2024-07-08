package smartin.miapi.modules.properties;

import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property manages the final durabiltiy of the item
 */
public class DurabilityProperty extends DoubleProperty {
    public static final String KEY = "durability";
    public static DurabilityProperty property;

    public DurabilityProperty() {
        super(KEY);
        property = this;
        allowVisualOnly = true;
    }
}
