package smartin.miapi.item.modular.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RarityProperty;

import java.util.List;
import java.util.UUID;

public class ExampleModularStrackableItem extends Item implements PlatformModularItemMethods,ModularItem {
    public static Item modularItem;

    public ExampleModularStrackableItem() {
        super(new Settings().maxCount(64));
        modularItem = this;
    }

    public ExampleModularStrackableItem(Item.Settings settings) {
        super(settings);
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

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        LoreProperty.appendLoreTop(stack, world, tooltip, context);
    }
}
