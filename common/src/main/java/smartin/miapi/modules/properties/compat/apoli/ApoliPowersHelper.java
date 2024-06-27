package smartin.miapi.modules.properties.compat.apoli;

import io.github.apace100.apoli.util.StackPowerUtil;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ApoliPowersHelper {

    public static List<StackPowerUtil.StackPower> getPowers(ItemStack itemStack, EquipmentSlot equipmentSlot, List<StackPowerUtil.StackPower> oldPowers) {
        ApoliPowersProperty.getPowerJson(itemStack).forEach(powerJson -> {
            if(powerJson.slot.equals(equipmentSlot)){
                oldPowers.add(getPower(powerJson));
            }
        });
        return oldPowers;
    }

    public static StackPowerUtil.StackPower getPower(ApoliPowersProperty.PowerJson powerJson) {
        StackPowerUtil.StackPower power = new StackPowerUtil.StackPower();
        power.powerId = powerJson.powerId;
        power.slot = powerJson.slot;
        power.isHidden = powerJson.isHidden;
        power.isNegative = powerJson.isNegative;
        return power;
    }
}
