package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantAbilityProperty extends DoubleProperty {
    //TODO:fully implement this
    public static String KEY = "enchantability";
    public static EnchantAbilityProperty property;

    public EnchantAbilityProperty() {
        super(KEY);
        property = this;
    }

    public static double getEnchantAbility(ItemStack itemStack) {
        List<ModuleInstance> moduleInstances = ItemModule.getModules(itemStack).allSubModules();
        List<Double> enchantAbilities = moduleInstances.stream().map(moduleInstance -> getEnchantAbility(moduleInstance)).sorted().collect(Collectors.toList());
        if (enchantAbilities.isEmpty()) {
            return 15.0;
        }
        if (enchantAbilities.size() > 1) {
            enchantAbilities.remove(0);
        }
        return enchantAbilities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(15.0);
    }

    public static double getEnchantAbility(ModuleInstance instance) {
        if (instance.getProperties().containsKey(property)) {
            return Math.max(1, property.getValueForModule(instance, 15.0));
        }
        Material material = MaterialProperty.getMaterial(instance);
        if (material != null) {
            return Math.max(1, material.getDouble("enchantability"));
        }
        return 15.0;
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
    }
}
