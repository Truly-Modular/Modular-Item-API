package smartin.miapi.mixin.client;


import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public class MinecraftMixin {


    /*
    @Redirect(method = "handleKeybinds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"
            )
    )
    private void redirectKeyUp(MultiPlayerGameMode instance, Player player) {
        if (!ClientKeybinding.isUsing) {
            instance.releaseUsingItem(player);
        }
    }

     */
}
