package smartin.miapi.mixin;

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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.item.modular.items.ModularSetableToolMaterial;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.HideFlagsProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = ItemStack.class, priority = 2000)
abstract class ItemStackMixin {

    @Shadow
    public abstract ItemStack setCustomName(Text name);

    @Inject(
            method = "getHideFlags()I",
            at = @At("TAIL"),
            cancellable = true)
    private void miapi$adjustGetHideFlags(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        cir.setReturnValue(HideFlagsProperty.getHideProperty(cir.getReturnValue(), stack));
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

    @Inject(
            method = "getItem",
            at = @At("RETURN"))
    private void miapi$getItemCallback(CallbackInfoReturnable<Item> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (cir.getReturnValue() instanceof ModularSetableToolMaterial toolMaterial) {
            //toolMaterial.setToolMaterial(stack);
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void miapi$modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof VisualModularItem) {
            cir.setReturnValue(ModularItem.getDurability(stack));
        }
    }

    @Inject(method = "isSuitableFor(Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    public void miapi$injectIsSuitable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(MiningLevelProperty.isSuitable(stack, state));
        }
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
        ReplaceOption.setHoverStack(stack, true);
        /*
        if (stack.getItem() instanceof ModularItem) {
            LoreProperty.property.appendLoreBottom(arg1, stack);
        }

         */
        LoreProperty.property.appendLoreBottom(arg1, stack);
    }

    @Inject(method = "isIn", at = @At("TAIL"), cancellable = true)
    public void miapi$injectItemTag(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            if (!cir.getReturnValue()) {
                cir.setReturnValue(FakeItemTagProperty.hasTag(tag.id(), stack));
            }
        }
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
