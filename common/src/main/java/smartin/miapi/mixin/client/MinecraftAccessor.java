package smartin.miapi.mixin.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("rightClickDelay")
    int getMiapiRightClickDelay();

    @Accessor("rightClickDelay")
    void setMiapiRightClickDelay(int rightClickDelay);
}
