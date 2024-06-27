package smartin.miapi.modules.properties.compat.better_combat;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import dev.architectury.platform.Platform;
import net.bettercombat.api.AttributesContainer;
import net.bettercombat.api.WeaponAttributesHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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
        ModularItemCache.setSupplier(BetterCombatProperty.KEY, itemStack -> new AttributeHolder(BetterCombatHelper.getAttributesContainer(itemStack)));
    }

    private static float getAttackRange(ItemStack stack) {
        if (Platform.isForgeLike()) {
            return (float) (AttributeProperty.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, AttributeRegistry.ATTACK_RANGE.getDefaultValue()) - 0.5f);
        }
        return (float) (AttributeProperty.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, AttributeRegistry.ATTACK_RANGE.getDefaultValue()) + 2.5f);
    }

    public static net.bettercombat.api.WeaponAttributes getAttributesContainer(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes attributes = container(ItemModule.getMergedProperty(stack, BetterCombatProperty.property));
            if (attributes != null) {
                attributes = new net.bettercombat.api.WeaponAttributes((attributes.attackRange() + getAttackRange(stack)) / 2, attributes.pose(), attributes.offHandPose(), attributes.isTwoHanded(), attributes.category(), attributes.attacks());
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
        return net.bettercombat.logic.WeaponRegistry.resolveAttributes(ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "modular_item"), WeaponAttributesHelper.decode(jsonReader));
    }

    public static ItemStack applyNBT(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes container = getAttributesContainer(itemStack);
            //TODO: rework this once better combat is available for 1.21
            if (container != null) {
                WeaponAttributesHelper.writeToNBT(itemStack, new AttributesContainer(null, container));
            } else {
            }
        }
        return itemStack;
    }

    @Nullable
    public static net.bettercombat.api.WeaponAttributes getAttributes(ItemStack stack) {
        return ModularItemCache.get(stack, BetterCombatProperty.KEY, new AttributeHolder(null)).attributes();
    }

    public record AttributeHolder(net.bettercombat.api.WeaponAttributes attributes) {
    }
}
