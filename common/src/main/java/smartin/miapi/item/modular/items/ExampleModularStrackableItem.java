package smartin.miapi.item.modular.items;

import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ExampleModularStrackableItem extends Item implements PlatformModularItemMethods,ModularItem {
    public static Item modularItem;

    public ExampleModularStrackableItem() {
        super(new Properties().stacksTo(64));
        modularItem = this;
    }

    public ExampleModularStrackableItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }



    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }
}
