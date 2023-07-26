package smartin.miapi.modules.properties.compat;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.AttributeProperty;

import java.io.StringReader;

public class BetterCombatHelper {
    public static void setup() {
        ModularItemCache.setSupplier(BetterCombatProperty.KEY, BetterCombatHelper::getContainer);
    }

    private static net.bettercombat.api.WeaponAttributes getContainer(ItemStack stack) {
        return container(ItemModule.getMergedProperty(stack, BetterCombatProperty.property));
    }

    private static float getAttackRange(ItemStack stack) {
        AttributeProperty.getAttributeModifiersRaw(stack);
        double rangeSave = AttributeProperty.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, 0) + 2.5f;
        return (float) rangeSave;
    }

    public static net.bettercombat.api.WeaponAttributes container(JsonElement data) {
        if (data == null) {
            return null;
        }
        String jsonString = data.toString();
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        net.bettercombat.api.WeaponAttributes attributes = net.bettercombat.logic.WeaponRegistry.resolveAttributes(new Identifier(Miapi.MOD_ID, "modular_item"), net.bettercombat.api.WeaponAttributesHelper.decode(jsonReader));
        return attributes;
    }

    public static net.bettercombat.api.WeaponAttributes getAttributes(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes attributes = (net.bettercombat.api.WeaponAttributes) ModularItemCache.get(stack, BetterCombatProperty.KEY);
            if (attributes != null) {
                attributes = new net.bettercombat.api.WeaponAttributes(getAttackRange(stack), attributes.pose(), attributes.offHandPose(), attributes.isTwoHanded(), attributes.category(), attributes.attacks());
            }
            return attributes;
        } else {
            return null;
        }
    }
}
