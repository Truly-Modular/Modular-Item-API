package smartin.miapi.modules.properties;

import smartin.miapi.modules.properties.util.BooleanProperty;

public class CanChildBeEmpty extends BooleanProperty {
    public static String KEY = "can_child_be_empty";
    public static CanChildBeEmpty property;

    public CanChildBeEmpty() {
        super(KEY, true);
        property = this;
    }
}
