package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.attributes.AttributeUtil;

@Environment(EnvType.CLIENT)
public class DpsStatDisplay extends SingleStatDisplayDouble {

    public DpsStatDisplay() {
        super(0, 0, 51, 19,
                (stack) -> Component.translatable("miapi.stat.miapi.dps"),
                (stack) -> Component.translatable("miapi.stat.miapi.dps.description"));
        this.maxValue = 25;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        if (hasAttackDamage(original) || hasAttackDamage(compareTo)) {
            super.shouldRender(original, compareTo);
            return true;
        }
        return false;
    }

    private boolean hasAttackDamage(ItemStack itemStack) {
        return AttributeUtil.getActualValue(itemStack, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE.value()) != 0;
    }

    @Override
    public double getValue(ItemStack stack) {
        double attackDamage = AttributeRegistry.getAttribute(stack, Attributes.ATTACK_DAMAGE.value(), EquipmentSlot.MAINHAND, 1);
        double attackSpeed = AttributeRegistry.getAttribute(stack, Attributes.ATTACK_SPEED.value(), EquipmentSlot.MAINHAND, 4);
        return attackDamage * attackSpeed;
    }
}
