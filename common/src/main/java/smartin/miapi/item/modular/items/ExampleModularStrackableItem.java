package smartin.miapi.item.modular.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.List;

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
    public void appendTooltip(ItemStack itemStack, TooltipContext tooltipContext, List<Text> list, TooltipType tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }
}
