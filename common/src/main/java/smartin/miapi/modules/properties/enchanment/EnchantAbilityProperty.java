package smartin.miapi.modules.properties.enchanment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.DoubleProperty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This property determines the enchantability of an item, which affects how good enchantments can be obtained from an enchanting table.
 * By default, enchantability is controlled by material values, but this property allows for custom enchantability values through module instances.
 * @header Enchantability Property
 * @description_start
 * The Enchantability Property defines how effective an item is for enchanting purposes.
 * This value influences the quality of enchantments available through the enchanting table.
 * By default, enchantability is derived from the material properties of the item, but this property allows for modification through module instances.
 * If no specific value is set, a default enchantability of 15 is used.
 * @path /data_types/properties/enchantments/enchantability
 * @data enchantability: double, representing the enchantability value used for enchantment calculations.
 * @data default_value: 15.0, the default enchantability if no custom value is provided.
 * @data material_controlled: By default, enchantability is determined by material properties through the `MaterialProperty` module.
 * @data module_instance: Allows for custom enchantability values through module instances, which are averaged if multiple values are present.
 */

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
