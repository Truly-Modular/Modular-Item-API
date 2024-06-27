package smartin.miapi.item.modular.items;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RarityProperty;
import smartin.miapi.modules.properties.RepairPriority;

import java.util.List;

public class ModularVisualOnlyItem extends Item implements PlatformModularItemMethods, VisualModularItem {
    public ModularVisualOnlyItem() {
        super(new Settings().maxCount(1).maxDamage(1000));
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return 0;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return Color.RED.argb();
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable("miapi.broken_item.name", DisplayNameProperty.getDisplayText(stack));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext tooltipContext, List<Text> list, TooltipType tooltipType) {
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }
}
