package smartin.miapi.item.modular.items;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Wearable;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.DisplayNameProperty;

public class ModularBoots extends ArmorItem implements ModularItem, Wearable {
    public ModularBoots() {
        super(new ModularArmorMaterial(),EquipmentSlot.FEET,new Settings());
    }

    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    static class ModularArmorMaterial implements ArmorMaterial {

        @Override
        public int getDurability(EquipmentSlot slot) {
            return 0;
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return null;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }
    }
}
