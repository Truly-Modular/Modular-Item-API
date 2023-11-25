package smartin.miapi.modules.properties;

import net.minecraft.item.ItemStack;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.List;
import java.util.stream.Collectors;

public class EnchantAbilityProperty {
    //TODO:fully implement this
    public static String KEY = "enchantability";
    public static EnchantAbilityProperty property;

    public EnchantAbilityProperty() {
        property = this;
    }

    public static double getEnchantAbility(ItemStack itemStack){
        List<ItemModule.ModuleInstance> moduleInstances = ItemModule.getModules(itemStack).allSubModules();
        List<Double> enchantAbilities = moduleInstances.stream().map(moduleInstance -> getEnchantAbility(moduleInstance)).sorted().collect(Collectors.toList());
        if(enchantAbilities.isEmpty()){
            return 1.0;
        }
        if(enchantAbilities.size()>1){
            enchantAbilities.remove(0);
        }
        return enchantAbilities.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(1.0);
    }

    public static double getEnchantAbility(ItemModule.ModuleInstance instance){
        Material material = MaterialProperty.getMaterial(instance);
        if(material!=null){
            return Math.max(1,material.getDouble("enchantability"));
        }
        return 1.0;
    }
}
