package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import smartin.miapi.client.gui.crafting.PreviewManager;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.item.modular.items.ModularSetableToolMaterial;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.HideFlagsProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 2000)
abstract class ItemStackMixin {

    @ModifyReturnValue(
            method = "getHideFlags()I",
            at = @At("RETURN"))
    private int miapi$adjustGetHideFlags(int original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            return HideFlagsProperty.getHideProperty(original, stack);
        }
        return original;
    }

    @Inject(
            method = "setNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("TAIL"),
            cancellable = true)
    private void miapi$cacheMaintanaince(NbtCompound nbt, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        ModularItemCache.clearUUIDFor(stack);
    }

    @Inject(
            method = "setNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void miapi$cacheMaintanaince2(NbtCompound nbt, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        ModularItemCache.clearUUIDFor(stack);
    }

    @ModifyReturnValue(
            method = "getItem",
            at = @At("RETURN"))
    private Item miapi$getItemCallback(Item original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (original instanceof ModularSetableToolMaterial toolMaterial) {
            toolMaterial.setToolMaterial(stack);
        }
        return original;
    }

    @ModifyReturnValue(method = "getMaxDamage", at = @At("RETURN"))
    public int miapi$modifyDurability(int original) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof VisualModularItem) {
            return ModularItem.getDurability(stack);
        }
        return original;
    }

    @ModifyReturnValue(method = "isSuitableFor(Lnet/minecraft/block/BlockState;)Z", at = @At("RETURN"))
    public boolean miapi$injectIsSuitable(boolean original,BlockState state) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            return MiningLevelProperty.isSuitable(stack, state);
        }
        return original;
    }

    @Inject(
            method = "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
            slice = @Slice(
                    from = @At("HEAD"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z",
                    shift = At.Shift.BEFORE,
                    by = +1
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void miapi$injectLoreTop(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> arg1) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!(stack.getItem() instanceof ModularItem)) {
            LoreProperty.property.injectTooltipOnNonModularItems(arg1, stack);
        }
    }

    @Inject(
            method = "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z"),
                    to = @At("RETURN")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z",
                    shift = At.Shift.BEFORE,
                    by = -1
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void miapi$injectLoreBottom(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> arg1) {
        ItemStack stack = (ItemStack) (Object) this;
        PreviewManager.setCursorItemstack(stack);
        /*
        if (stack.getItem() instanceof ModularItem) {
            LoreProperty.property.appendLoreBottom(arg1, stack);
        }

         */
        LoreProperty.property.appendLoreBottom(arg1, stack);
    }

    @ModifyReturnValue(method = "isIn", at = @At("RETURN"))
    public boolean miapi$injectItemTag(boolean original, TagKey<Item> tag) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            if (!original) {
                return FakeItemTagProperty.hasTag(tag.id(), stack);
            }
        }
        return original;
    }

    @Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    public <T extends LivingEntity> void miapi$takeDurabilityDamage(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!MiapiConfig.INSTANCE.server.other.fullBreakModularItems && stack.getItem() instanceof VisualModularItem && stack.isDamageable() && stack.getDamage() + amount + 1 >= stack.getMaxDamage()) {
            for (EquipmentSlot value : EquipmentSlot.values()) {
                if (entity.getEquippedStack(value).equals(stack)) {
                    ItemStack brokenStack = new ItemStack(RegistryInventory.visualOnlymodularItem);
                    brokenStack.setNbt(stack.getNbt());
                    entity.equipStack(value, brokenStack);
                    ci.cancel();
                }
            }
        }
    }
}
