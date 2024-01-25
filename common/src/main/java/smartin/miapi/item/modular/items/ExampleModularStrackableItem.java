package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.RarityProperty;

import java.util.UUID;

public class ExampleModularStrackableItem extends Item implements ModularItem {
    public static Item modularItem;

    public ExampleModularStrackableItem() {
        super(new Settings().maxCount(64));
        modularItem = this;
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    /**
     * Accessor for Properties
     */
    public static UUID attackDamageUUID() {
        return ATTACK_DAMAGE_MODIFIER_ID;
    }

    public static UUID attackSpeedUUID() {
        return ATTACK_SPEED_MODIFIER_ID;
    }
}
