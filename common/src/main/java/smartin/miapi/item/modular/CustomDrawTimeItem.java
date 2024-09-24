package smartin.miapi.item.modular;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.attributes.AttributeUtil;

public interface CustomDrawTimeItem {
    double getBaseDrawTime(ItemStack itemStack);

    default double getActualDrawTime(ItemStack stack){
        return getBaseDrawTime(stack) - AttributeUtil.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.DRAW_TIME.value());
    }
}
