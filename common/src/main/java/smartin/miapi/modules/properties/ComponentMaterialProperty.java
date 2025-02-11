package smartin.miapi.modules.properties;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.Composite;
import smartin.miapi.material.composite.CompositeMaterial;
import smartin.miapi.material.composite.material.CompositeFromOtherMaterial;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

public class ComponentMaterialProperty extends CodecProperty<List<Composite>> implements ComponentApplyProperty {
    public static ResourceLocation KEY = Miapi.id("material_component_property");

    public ComponentMaterialProperty() {
        super(Composite.CODEC.listOf());
    }

    @Override
    public List<Composite> initialize(List<Composite> data, ModuleInstance context) {
        List<Composite> init = new ArrayList<>();
        Material material = MaterialProperty.getMaterial(context);
        data.forEach(c -> {
            if (
                    material != null &&
                    c instanceof CompositeFromOtherMaterial otherMaterialComposite
            ) {
                otherMaterialComposite.setMaterial(material);
            }
            init.add(c);
        });
        return init;
    }

    @Override
    public List<Composite> merge(List<Composite> left, List<Composite> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {

        if (getData(itemStack).isPresent()) {
            try {
                itemStack.set(CompositeMaterial.COMPOSITE_MATERIAL_COMPONENT, CompositeMaterial.getFromComposites(getData(itemStack).get()));
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("failure", e);
            }
        }
    }
}
