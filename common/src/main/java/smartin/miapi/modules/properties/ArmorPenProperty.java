package smartin.miapi.modules.properties;

import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class ArmorPenProperty extends SimpleDoubleProperty {
    public static final String KEY = "armorPen";
    public static ArmorPenProperty property;
    protected ArmorPenProperty() {
        super(KEY);
        property = this;
        //TODO:this
    }
}
