package smartin.miapi.client.gui.crafting.statdisplay;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.attributes.AttributeRegistry;

@Environment(EnvType.CLIENT)
public class DpsStatDisplay extends SingleStatDisplayDouble{

    public DpsStatDisplay() {
        super(0, 0, 51, 19, (stack)->Text.literal("DPS"), (stack)->Text.empty());
        this.maxValue = 25;
    }

    @Override
    public boolean shouldRender(ItemStack original, ItemStack compareTo) {
        super.shouldRender(original,compareTo);
        return true;
    }

    @Override
    public double getValue(ItemStack stack) {
        double attackDamage = AttributeRegistry.getAttribute(stack, EntityAttributes.GENERIC_ATTACK_DAMAGE, EquipmentSlot.MAINHAND, 1);
        double attackSpeed = AttributeRegistry.getAttribute(stack, EntityAttributes.GENERIC_ATTACK_SPEED, EquipmentSlot.MAINHAND, 4);
        return attackDamage * attackSpeed;
    }
}
