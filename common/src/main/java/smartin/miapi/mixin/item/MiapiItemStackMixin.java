package smartin.miapi.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.MixinContextFlags;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.FakeItemManager;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.properties.AssumeItemIdentityProperty;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static smartin.miapi.item.modular.ModularItem.isModularItem;
import static smartin.miapi.item.modular.ModularItem.isModularItemNoComponent;

@Mixin(value = ItemStack.class, priority = 2000)
public abstract class MiapiItemStackMixin {

    @ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
    public boolean miapi$injectItemTag(boolean original, TagKey<Item> tag) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isModularItemNoComponent(stack)) {
            if (!original) {
                return FakeItemTagProperty.hasTag(tag.location(), stack);
            }
        }
        return original;
    }

    @Inject(
            method = "Lnet/minecraft/world/item/ItemStack;getItemHolder()Lnet/minecraft/core/Holder;",
            at = @At("HEAD"))
    public void miapi$preventItem(CallbackInfoReturnable<Item> cir) {
    }

    @Inject(method = "getItem", at = @At("TAIL"))
    public void miapi$capturePotentialItemstack(CallbackInfoReturnable<Item> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isModularItemNoComponent(cir.getReturnValue())) {
            FakeItemManager.getItemCall(stack, cir.getReturnValue());
        }
    }

    @ModifyReturnValue(method = "getItem", at = @At("RETURN"))
    public Item miapi$adjustIsItem(Item original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItem.isModularItemNoComponent(original)) {
            var a = MixinContextFlags.IGNORE_NEXT_GET_ITEM_CALL;
            Item fake = a.get().get(stack);
            if (fake != null) {
                return fake;
            }
        }
        return original;
    }

    @Inject(method = "copy", at = @At("RETURN"))
    public void miapi$keepLookupOnCopy(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItemStackConverter.lookupMap.containsKey(stack)) {
            ModularItemStackConverter.lookupMap.put(cir.getReturnValue(), ModularItemStackConverter.lookupMap.get(stack));
        }
    }

    @ModifyReturnValue(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("RETURN"))
    public boolean miapi$adjustIsItem(boolean original, Item item) {
        ItemStack stack = (ItemStack) (Object) this;
        if (item != null && !original && isModularItemNoComponent(stack)) {
            return AssumeItemIdentityProperty.isItem(original, item, stack);
        }
        return original;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    public void miapi$capturePotentialItemstack(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (isModularItem(stack, item.asItem())) {
            FakeEnchantmentManager.initOnItemStack(stack);
        }
    }

    @Inject(method = "addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"))
    public <T> void miapi$injectToolTip(DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        PreviewManager.setCursorItemstack(stack);
        if (DataComponents.UNBREAKABLE.equals(component)) {
            FakeEnchantmentManager.initOnItemStack(stack);
            if (VisualModularItem.isVisualModularItem(stack)) {
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

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void miapi$preventFullBreak(int damage, ServerLevel level, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
        ItemStack current = (ItemStack) (Object) this;
        if (isModularItem(current) && current.isDamageableItem() && !MiapiConfig.getServerConfig().other.fullBreakModularItems) {
            if (player != null && !player.hasInfiniteMaterials()) {
                if (damage + current.getDamageValue() >= current.getMaxDamage()) {
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        if (player.getItemBySlot(slot).equals(current)) {
                            ItemStack broken = ModularItem.convertToBroken(current);
                            player.setItemSlot(slot, broken);
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
            at = @At("HEAD"),
            cancellable = true)
    public void miapi$preventFullBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack current = (ItemStack) (Object) this;
        if (isModularItem(current) && current.isDamageableItem() && !MiapiConfig.getServerConfig().other.fullBreakModularItems) {
            if (entity != null && !entity.hasInfiniteMaterials()) {
                if (amount + current.getDamageValue() >= current.getMaxDamage()) {
                    ItemStack broken = ModularItem.convertToBroken(current);
                    entity.setItemSlot(slot, broken);
                    ci.cancel();
                }
            }
        }
    }
}
