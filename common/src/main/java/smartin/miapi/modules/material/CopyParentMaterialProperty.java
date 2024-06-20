package smartin.miapi.modules.material;

import smartin.miapi.modules.properties.util.BooleanProperty;

public class CopyParentMaterialProperty extends BooleanProperty {
    public static String KEY = "copyParentMaterial";
    public static CopyParentMaterialProperty property;

    public CopyParentMaterialProperty() {
        super(KEY, false);
        property = this;
    }
}
