package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 2000)
abstract class ItemStackMixin {

    @ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    public boolean miapi$injectItemTag(boolean original, TagKey<Item> tag) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItem.isModularItem(stack)) {
            if (!original) {
                return FakeItemTagProperty.hasTag(tag.location(), stack);
            }
        }
        return original;
    }

    @Inject(method = "getItem", at = @At("TAIL"))
    public void miapi$capturePotentialItemstack(CallbackInfoReturnable<Item> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItem.isModularItem(stack)) {
            FakeItemstackReferenceProvider.setReference(cir.getReturnValue(), stack);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    public void miapi$capturePotentialItemstack(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItem.isModularItem(stack)) {
            FakeEnchantmentManager.initOnItemStack(stack);
        }
    }

    @Inject(method = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"))
    public <T> void miapi$capturePotentialItemstack(DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (DataComponents.UNBREAKABLE.equals(component)) {
            FakeEnchantmentManager.initOnItemStack(stack);
            if (VisualModularItem.isModularItem(stack)) {
                List<Component> lore = new ArrayList<>();
                LoreProperty.property.appendLoreBottom(lore, stack);
                lore.forEach(tooltipAdder);
            } else {
                List<Component> lore = new ArrayList<>();
                LoreProperty.property.injectTooltipOnNonModularItems(lore, stack);
                lore.forEach(tooltipAdder);
            }
        }
    }
}
