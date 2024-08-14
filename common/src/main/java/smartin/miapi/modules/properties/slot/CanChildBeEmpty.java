package smartin.miapi.modules.properties.slot;

import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class CanChildBeEmpty extends ComplexBooleanProperty {
    public static String KEY = "can_child_be_empty";
    public static CanChildBeEmpty property;

    public CanChildBeEmpty() {
        super(KEY, true);
        property = this;
    }
}
