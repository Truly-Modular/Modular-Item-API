package smartin.miapi.material.composite.material;

import smartin.miapi.material.DefaultMaterial;
import smartin.miapi.material.base.Material;

public abstract class BasicOtherMaterialComposite implements CompositeFromOtherMaterial {
    public Material material;

    protected BasicOtherMaterialComposite() {
        this(new DefaultMaterial());
    }

    protected BasicOtherMaterialComposite(Material material) {
        this.material = material;
    }

    @Override
    public void setMaterial(Material material) {
        this.material = material;
    }

    @Override
    public Material getMaterial() {
        return material == null ? new DefaultMaterial() : material;
    }
}
