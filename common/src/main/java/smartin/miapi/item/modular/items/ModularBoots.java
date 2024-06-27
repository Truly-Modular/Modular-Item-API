package smartin.miapi.item.modular.items;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RarityProperty;
import smartin.miapi.modules.properties.RepairPriority;

import java.util.List;

public class ModularBoots extends ArmorItem implements PlatformModularItemMethods, ModularItem {
    public ModularBoots(Properties settings) {
        super(new ModularArmorMaterial(), Type.BOOTS, settings);
    }

    public ModularBoots() {
        super(new ModularArmorMaterial(), Type.BOOTS, new Properties());
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamageValue()) / ModularItem.getDurability(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip, net.minecraft.world.item.Item.TooltipContext context) {
        LoreProperty.appendLoreTop(stack, world, tooltip, context);
    }
}
