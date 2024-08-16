package smartin.miapi.modules.material;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class CopyParentMaterialProperty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("copy_parent_material");
    public static CopyParentMaterialProperty property;

    public CopyParentMaterialProperty() {
        super(KEY, false);
        property = this;
    }
}
