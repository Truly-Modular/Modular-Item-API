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
import smartin.miapi.material.composite.material.DatapackComposite;
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
        data = DatapackComposite.copy(data);
        data.forEach(c -> {
            if (
                    material != null &&
                    c instanceof CompositeFromOtherMaterial otherMaterialComposite
            ) {
                otherMaterialComposite.setMaterial(material);
            }
            init.add(c);
        });
        Miapi.LOGGER.info("module " + context.moduleID);
        if (context.parent != null) {
            Miapi.LOGGER.info("parent" + context.parent.getId());
        } else {
            Miapi.LOGGER.info("no parent");
        }
        if (material != null) {
            Miapi.LOGGER.info("material" + material.getID());
        }
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
                var data = getData(itemStack).get();
                itemStack.set(CompositeMaterial.COMPOSITE_MATERIAL_COMPONENT, CompositeMaterial.getFromComposites(data));
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("failure", e);
            }
        }
    }
}
