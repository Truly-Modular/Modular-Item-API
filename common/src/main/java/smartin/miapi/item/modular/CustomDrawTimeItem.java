package smartin.miapi.item.modular;

import net.minecraft.world.item.ItemStack;
import smartin.miapi.modules.properties.projectile.DrawTimeProperty;

public interface CustomDrawTimeItem {
    double getBaseDrawTime(ItemStack itemStack);

    default double getActualDrawTime(ItemStack stack){
        return DrawTimeProperty.property.getValue(stack).orElse(0.25);
    }
}
