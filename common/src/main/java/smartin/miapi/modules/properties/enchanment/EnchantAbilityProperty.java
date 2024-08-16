package smartin.miapi.modules.properties.enchanment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnchantAbilityProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("enchantability");
    public static EnchantAbilityProperty property;

    public EnchantAbilityProperty() {
        super(KEY);
        property = this;
    }

    public static double getEnchantAbility(ItemStack itemStack) {
        List<ModuleInstance> moduleInstances = ItemModule.getModules(itemStack).allSubModules();
        List<Double> enchantAbilities = moduleInstances.stream().map(EnchantAbilityProperty::getEnchantAbility).sorted().collect(Collectors.toList());
        if (enchantAbilities.isEmpty()) {
            return 15.0;
        }
        if (enchantAbilities.size() > 1) {
            enchantAbilities.removeFirst();
        }
        return enchantAbilities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(15.0);
    }

    public static double getEnchantAbility(ModuleInstance instance) {
        Optional<DoubleOperationResolvable> resolvableOptional = property.getData(instance);
        if (resolvableOptional.isPresent()) {
            return resolvableOptional.get().evaluate(0, 15);
        } else {
            Material material = MaterialProperty.getMaterial(instance);
            if (material != null) {
                return Math.max(1, material.getDouble("enchantability"));
            }
        }
        return 15.0;
    }
}
