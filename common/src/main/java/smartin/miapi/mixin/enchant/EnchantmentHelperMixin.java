package smartin.miapi.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.LuminousLearningProperty;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @Inject(method = {"Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"}, at = @At("RETURN"))
    private static void afterModifyEnchantments(ServerLevel level, ItemStack stack, int damage, CallbackInfoReturnable<Integer> cir) {
        if (ModularItem.isModularItem(stack)) {
            MiapiEvents.MODULAR_ITEM_DAMAGE.invoker().durability(damage, stack, level);
        }
    }

    // ---- 1) If the component is null but we have fakes, return a non-null placeholder
    @WrapOperation(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
            )
    )
    private static Object wrapGetEnchantments(
            ItemStack stack,
            DataComponentType<?> type,
            Operation<Object> original,
            ItemStack callStack, EquipmentSlot slot, LivingEntity entity
    ) {
        Object real = original.call(stack, type);

        // Only care about the ENCHANTMENTS component
        if (type == DataComponents.ENCHANTMENTS && real == null) {
            if (ModularItem.isModularItem(stack)) {
                // Return a non-null placeholder; EMPTY is fine since we'll also wrap isEmpty()
                return ItemEnchantments.EMPTY; // adapt to your version if constant name differs
            }
        }
        return real;
    }

    // ---- 2) If vanilla thinks it's empty, flip it to non-empty when we have fakes
    @WrapOperation(
            method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments;isEmpty()Z"
            )
    )
    private static boolean wrapIsEmpty(
            ItemEnchantments self,
            Operation<Boolean> original,
            ItemStack stack, EquipmentSlot slot, LivingEntity entity
    ) {
        boolean vanillaEmpty = original.call(self);
        if (vanillaEmpty && ModularItem.isModularItem(stack)) {
            return false;
        }
        return vanillaEmpty;
    }

    @ModifyReturnValue(method = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processBlockExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I", at = @At(value = "RETURN"))
    private static int miapi$adjustSupportedItem(int original, ServerLevel level, ItemStack stack, int experience) {
        return LuminousLearningProperty.property.getAdjustedXp(original, level, stack, experience);
    }

    /*
    @ModifyReturnValue(method = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processMobExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;I)I", at = @At(value = "RETURN"))
    private static int miapi$adjustSupportedItem(int original, ServerLevel level, @Nullable Entity killer, Entity mob, int experience) {
        return LuminousLearningProperty.property.getAdjustedXp(original, level, stack, experience);
    }
     */
}
