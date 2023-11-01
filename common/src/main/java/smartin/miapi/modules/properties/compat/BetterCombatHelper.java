package smartin.miapi.modules.properties.compat;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import dev.architectury.platform.Platform;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.AttributeProperty;

import java.io.StringReader;

public class BetterCombatHelper {
    public static void setup() {
        ModularItemCache.setSupplier(BetterCombatProperty.KEY, BetterCombatHelper::getAttributesContainer);
    }

    private static float getAttackRange(ItemStack stack) {
        if (Platform.isForge()) {
            return (float) (AttributeProperty.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, 0) - 0.5f);
        }
        return (float) (AttributeProperty.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, 0) + 2.5f);
    }

    private static net.bettercombat.api.WeaponAttributes getAttributesContainer(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes attributes = container(ItemModule.getMergedProperty(stack, BetterCombatProperty.property));
            if (attributes != null) {
                attributes = new net.bettercombat.api.WeaponAttributes(getAttackRange(stack), attributes.pose(), attributes.offHandPose(), attributes.isTwoHanded(), attributes.category(), attributes.attacks());
            }
            return attributes;
        } else {
            return null;
        }
    }

    private static net.bettercombat.api.WeaponAttributes container(JsonElement data) {
        if (data == null) {
            return null;
        }
        String jsonString = data.toString();
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        return net.bettercombat.logic.WeaponRegistry.resolveAttributes(new Identifier(Miapi.MOD_ID, "modular_item"), net.bettercombat.api.WeaponAttributesHelper.decode(jsonReader));
    }

    @Nullable
    public static net.bettercombat.api.WeaponAttributes getAttributes(ItemStack stack) {
        return ModularItemCache.getRaw(stack, BetterCombatProperty.KEY);
    }
}
