package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.attributes.AttributeRegistry;

@Environment(EnvType.CLIENT)
public class DpsStatDisplay extends SingleStatDisplayDouble {

    public DpsStatDisplay() {
        super(0, 0, 51, 19,
                (stack) -> Component.translatable("miapi.stat.dps"),
                (stack) -> Component.translatable("miapi.stat.dps.description"));
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
        return itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
    }

    @Override
    public double getValue(ItemStack stack) {
        double attackDamage = AttributeRegistry.getAttribute(stack, Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, 1);
        double attackSpeed = AttributeRegistry.getAttribute(stack, Attributes.ATTACK_SPEED, EquipmentSlot.MAINHAND, 4);
        return attackDamage * attackSpeed;
    }
}
