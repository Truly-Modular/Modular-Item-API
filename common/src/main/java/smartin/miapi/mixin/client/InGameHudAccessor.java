package smartin.miapi.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Gui.class)
public interface InGameHudAccessor {
    @Invoker
    Player callGetCameraPlayer();

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
