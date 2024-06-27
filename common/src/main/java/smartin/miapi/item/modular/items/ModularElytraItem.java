package smartin.miapi.item.modular.items;

import dev.architectury.injectables.annotations.ExpectPlatform;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RarityProperty;
import smartin.miapi.modules.properties.RepairPriority;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ModularElytraItem extends ArmorItem implements PlatformModularItemMethods, ModularItem {
    public ModularElytraItem(Properties settings) {
        super(new ModularArmorMaterial(), Type.CHESTPLATE, settings);
    }

    @ExpectPlatform
    public static ModularElytraItem getInstance() {
        return new ModularElytraItem(new Properties().stacksTo(1).fireResistant());
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
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float)stack.getDamageValue()) / ModularItem.getDurability(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(stack, list, tooltipContext, tooltipType);
    }
}
