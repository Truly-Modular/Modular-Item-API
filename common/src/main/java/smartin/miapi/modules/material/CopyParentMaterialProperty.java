package smartin.miapi.modules.material;

import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class CopyParentMaterialProperty extends ComplexBooleanProperty {
    public static String KEY = "copy_parent_material";
    public static CopyParentMaterialProperty property;

    public CopyParentMaterialProperty() {
        super(KEY, false);
        property = this;
    }
}
