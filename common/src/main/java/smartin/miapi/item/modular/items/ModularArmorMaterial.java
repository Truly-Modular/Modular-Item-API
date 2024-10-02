package smartin.miapi.item.modular.items;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.DurabilityProperty;
import smartin.miapi.modules.properties.EnchantAbilityProperty;
import smartin.miapi.modules.properties.EquipmentSlotProperty;

public class ModularArmorMaterial implements ArmorMaterial {
    @Override
    public int getDurability(ArmorItem.Type type) {
        return 50;
    }

    @Override
    public int getProtection(ArmorItem.Type type) {
        return 0;
    }

    @Override
    public int getEnchantability() {
        return 15;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvent.of(new Identifier("silent"));
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return "miapi_modular_armor";
    }

    @Override
    public float getToughness() {
        return 0;
    }

    @Override
    public float getKnockbackResistance() {
        return 0;
    }

    public static ArmorMaterial forItem(ItemStack itemStack) {
        return new ArmorMaterial() {
            @Override
            public int getDurability(ArmorItem.Type type) {
                return DurabilityProperty.property.getValue(itemStack).intValue();
            }

            @Override
            public int getProtection(ArmorItem.Type type) {
                return (int) AttributeProperty.getActualValueCache(itemStack, type.getEquipmentSlot(), EntityAttributes.GENERIC_ARMOR, 0.0);
            }

            @Override
            public int getEnchantability() {
                return (int)EnchantAbilityProperty.getEnchantAbility(itemStack);
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvent.of(new Identifier("silent"));
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.EMPTY;
            }

            @Override
            public String getName() {
                return "miapi_runtime_fake_armor";
            }

            @Override
            public float getToughness() {
                return (int) AttributeProperty.getActualValueCache(itemStack, EquipmentSlotProperty.getSlot(itemStack), EntityAttributes.GENERIC_ARMOR, 0.0);
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        };
    }
}
