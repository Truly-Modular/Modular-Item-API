package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.IngredientController;
import smartin.miapi.material.base.Material;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @header Repair Priority Property
 * @path /data_types/properties/repair_priority
 * @description_start The RepairPriority property controls the materials used to repair an item and assigns a priority value to these
 * materials. This value determines how effectively the item can be repaired using different materials.
 * <p>
 * The priority value is a double, where a higher value represents a more effective repair material. The system
 * maintains a list of materials that can be used for repair, and the highest priority material is used when
 * repairing an item. If multiple materials have the same priority, all of them can be used interchangeably.
 * @description_end
 * @data repair_priority: A double value that determines the priority of repair materials for the item.
 */

public class RepairPriority extends DoubleProperty {
    public static RepairPriority property;
    public static final ResourceLocation KEY = Miapi.id("repair_priority");

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
            highestValue = Math.max(highestValue, material1.getRepairValueOfItem(material));
        }
        return highestValue;
    }

    public static Ingredient getRepairIngredient(ItemStack itemStack) {
        return IngredientController.mergeIngredients(property.getRepairMaterials(itemStack).stream().map(IngredientController::getRepairIngredient));
    }


    private List<Material> getRepairMaterialsPrivate(ItemStack itemStack) {
        double lowest = Double.MAX_VALUE;
        List<Material> materials = new ArrayList<>();
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            Optional<Double> optional = getValue(itemStack);
            if (optional.isPresent()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null && lowest > optional.get()) {
                    lowest = optional.get();
                }
            }
        }
        for (ModuleInstance moduleInstance : ItemModule.getModules(itemStack).allSubModules()) {
            Optional<Double> optional = getValue(itemStack);
            if (optional.isPresent()) {
                Material material = MaterialProperty.getMaterial(moduleInstance);
                if (material != null && Math.abs(lowest - optional.get()) < 0.001) {
                    materials.add(material);
                }
            }
        }
        return materials;
    }

    public Optional<DoubleOperationResolvable> getData(ItemStack itemStack) {
        if (itemStack == null) {
            return Optional.empty();
        }
        if (!VisualModularItem.isVisualModularItem(itemStack)) {
            return Optional.empty();
        }
        if (ReloadEvents.isInReload()) {
            return Optional.empty();
        }
        ModuleInstance baseModule = ItemModule.getModules(itemStack);
        if (baseModule == null || baseModule.getModule() == ItemModule.empty) {
            return Optional.empty();
        }
        return Optional.ofNullable(baseModule.getPropertyItemStack(this));
    }
}
