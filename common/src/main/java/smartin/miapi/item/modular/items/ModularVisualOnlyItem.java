package smartin.miapi.item.modular.items;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RepairPriority;

import java.util.List;

public class ModularVisualOnlyItem extends Item implements PlatformModularItemMethods, VisualModularItem {
    public ModularVisualOnlyItem() {
        super(new Properties().stacksTo(1).durability(1000));
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Color.RED.argb();
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("miapi.broken_item.name", DisplayNameProperty.getDisplayText(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }
}
