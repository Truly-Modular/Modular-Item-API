package smartin.miapi.item.modular.items;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularChestPlate extends ArmorItem implements ModularItem {
    public ModularChestPlate() {
        super(new ModularArmorMaterial(), Type.CHESTPLATE,new Settings());
        ElytraItem elytraItem;
        LivingEntity livingEntity;
        PlayerEntity player;
        ClientPlayerEntity clientPlayerEntity;
        FireworkRocketEntity fireworkRocketEntity;
        FireworkRocketItem fireworkRocketItem;
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack){
        return true;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }
}
