package smartin.miapi.material.composite.material;

import smartin.miapi.material.DefaultMaterial;
import smartin.miapi.material.base.Material;

public abstract class BasicOtherMaterialComposite implements CompositeFromOtherMaterial {
    public Material material;
    public boolean overWriteAble;

    protected BasicOtherMaterialComposite() {
        this(new DefaultMaterial());
    }

    protected BasicOtherMaterialComposite(Material material) {
        this.material = material;
    }

    @Override
    public void setMaterial(Material material) {
        if (getMaterial() instanceof DefaultMaterial || overWriteAble) {
            this.material = material;
        }
    }

    public void setOverWriteAble(boolean overWriteAble) {
        this.overWriteAble = overWriteAble;
    }

    public boolean getOverWriteAble() {
        return overWriteAble;
    }

    @Override
    public Material getMaterial() {
        return material == null ? new DefaultMaterial() : material;
    }
}
