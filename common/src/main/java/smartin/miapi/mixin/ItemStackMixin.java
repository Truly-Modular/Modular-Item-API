package smartin.miapi.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.edit_options.ReplaceOption;
import smartin.miapi.modules.properties.FakeItemTagProperty;
import smartin.miapi.modules.properties.HideFlagsProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 2000)
public abstract class ItemStackMixin {

    @Shadow
    @Final
    private static String HIDE_FLAGS_KEY;

    @Shadow
    public abstract ItemStack setCustomName(@Nullable Text name);

    //@Inject(method = "foo()V", at = @At(value = "INVOKE", item = "La/b/c/Something;doSomething()V", shift = At.Shift.AFTER))
    @Inject(
            method = "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void miapi$skipAttributeModifier(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List list, MutableText mutableText, int i, EquipmentSlot[] var6, int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap) {
        //
    }

    @Inject(
            method = "getHideFlags()I",
            at = @At("TAIL"),
            cancellable = true)
    private void miapi$adjustGetHideFlags(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        cir.setReturnValue(HideFlagsProperty.getHideProperty(cir.getReturnValue(), stack));
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void miapi$modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
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
}
