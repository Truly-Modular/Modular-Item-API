package smartin.miapi.mixin;

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
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.AssumeItemIdentityProperty;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.FakeEnchantmentManager;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 2000)
public abstract class MiapiItemStackMixin {

    @Shadow
    public abstract void releaseUsing(Level level, LivingEntity livingEntity, int timeLeft);

    @Shadow
    public abstract boolean is(Item item);

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
        if (ModularItem.isModularItem(stack, cir.getReturnValue())) {
            FakeItemstackReferenceProvider.setReference(cir.getReturnValue(), stack);
        }
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
        if (!original && ModularItem.isModularItem(stack)) {
            var property = AssumeItemIdentityProperty.property.getData(stack);
            var match = property.map(a -> a.stream().anyMatch(h -> h.value().equals(item)));
            return match.orElse(original);
        }
        return original;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    public void miapi$capturePotentialItemstack(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (ModularItem.isModularItem(stack, item.asItem())) {
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
        if (ModularItem.isModularItem(current) && current.isDamageableItem() && !MiapiConfig.INSTANCE.server.other.fullBreakModularItems) {
            if (player != null && !player.hasInfiniteMaterials()) {
                if (damage + current.getDamageValue() >= current.getMaxDamage()) {
                    for (EquipmentSlot slot : EquipmentSlot.values()) {
                        if (player.getItemBySlot(slot).equals(current)) {
                            ItemStack broken = new ItemStack(RegistryInventory.visualOnlymodularItem);
                            ItemModule.getModules(current).writeToItem(broken);
                            broken.set(DataComponents.DAMAGE, current.get(DataComponents.DAMAGE));
                            broken.set(DataComponents.MAX_DAMAGE, current.get(DataComponents.MAX_DAMAGE));
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
    public <T> void miapi$preventFullBreak(int amount, LivingEntity entity, EquipmentSlot slot, CallbackInfo ci) {
        ItemStack current = (ItemStack) (Object) this;
        if (ModularItem.isModularItem(current) && current.isDamageableItem() && !MiapiConfig.INSTANCE.server.other.fullBreakModularItems) {
            if (entity != null && !entity.hasInfiniteMaterials()) {
                if (amount + current.getDamageValue() >= current.getMaxDamage()) {
                    ItemStack broken = new ItemStack(RegistryInventory.visualOnlymodularItem);
                    ItemModule.getModules(current).writeToItem(broken);
                    broken.set(DataComponents.DAMAGE, current.get(DataComponents.DAMAGE));
                    broken.set(DataComponents.MAX_DAMAGE, current.get(DataComponents.MAX_DAMAGE));
                    entity.setItemSlot(slot, broken);
                    ci.cancel();
                }
            }
        }
    }
}
