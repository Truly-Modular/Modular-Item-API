package smartin.miapi.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PoseStack.Pose.class)
public interface PoseAccessor {
    @Invoker("computeNormalMatrix")
    void miapi$callComputeNormalMatrix();
}
