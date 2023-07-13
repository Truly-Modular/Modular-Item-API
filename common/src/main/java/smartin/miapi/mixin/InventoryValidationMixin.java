package smartin.miapi.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Inventory.class)
interface InventoryValidationMixin {
  /*
  TODO:Fix this
  @Inject(
      method = "Lnet/minecraft/inventory/Inventory;canPlayerUse(Lnet/minecraft/entity/player/PlayerEntity;)Z",
      require = 1, allow = 1,
      at = @At(shift = At.Shift.BEFORE, value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;getX()I"),
      locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
  private static void checkWithinActualReach(final BlockEntity blockEntity, final PlayerEntity player, final int reachDistance, final CallbackInfoReturnable<Boolean> cir, final World world, final BlockPos pos) {
    if (player.squaredDistanceTo(pos.toCenterPos()) <= ReachEntityAttributes.getSquaredReachDistance(player, reachDistance * reachDistance)) {
      cir.setReturnValue(true);
    }
  }
   */
}
