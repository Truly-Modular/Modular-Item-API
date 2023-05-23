package smartin.miapi.modules.properties;

import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class BlockProperty extends SimpleDoubleProperty implements ModuleProperty {
    public static final String KEY = "blocking";
    public static BlockProperty property;

    public BlockProperty() {
        super(KEY);
        property = this;
    }

}
