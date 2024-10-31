package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * controls the repair material
 */
public class RepairPriority extends DoubleProperty {
    public static RepairPriority property;
    public static final String KEY = "repairPriority";

    public RepairPriority() {
        super(KEY);
        property = this;
        allowVisualOnly = true;
        ModularItemCache.setSupplier(KEY + "_materials", this::getRepairMaterialsPrivate);
    }

    public List<Material> getRepairMaterials(ItemStack itemStack) {
        return ModularItemCache.getVisualOnlyCache(itemStack, KEY + "_materials", new ArrayList<>());
    }

    public static double getRepairValue(ItemStack tool, ItemStack material) {
        double highestValue = 0;
        for (Material material1 : property.getRepairMaterials(tool)) {
            highestValue = Math.max(highestValue, material1.getValueOfItem(material));
        }
        return highestValue;
    }

    public static Ingredient asRepairIngredient(ItemStack tool) {
        for (Material repairing : property.getRepairMaterials(tool)) {
            Ingredient found = repairing.getIngredient();
            if (found != null) {
                return found;
            }
        }
        return Ingredient.EMPTY;
    }


    private List<Material> getRepairMaterialsPrivate(ItemStack itemStack) {
        double lowest = Double.MAX_VALUE;
        List<Material> materials = new ArrayList<>();
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            Double value = getValueForModule(moduleInstance, null);
            Material material = MaterialProperty.getMaterial(moduleInstance);
            if (value != null && material != null && lowest > value) {
                lowest = value;
            }
        }
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            Double value = getValueForModule(moduleInstance, null);
            Material material = MaterialProperty.getMaterial(moduleInstance);
            if (value != null && material != null && Math.abs(lowest - value) < 0.001) {
                materials.add(material);
            }
        }
        return materials;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return null;
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return 0;
    }
}
