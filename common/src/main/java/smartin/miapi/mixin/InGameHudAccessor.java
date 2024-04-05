package smartin.miapi.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
    @Invoker
    PlayerEntity callGetCameraPlayer();

    @Invoker
    int callGetHeartCount(LivingEntity entity);

    @Invoker
    int callGetHeartRows(int heartCount);

    @Accessor
    int getScaledHeight();

    @Accessor
    int getScaledWidth();

    @Accessor
    int getRenderHealthValue();
}
