package smartin.miapi.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.client.model.MiapiItemModel;

@Mixin(PoseStack.class)
public class PoseStackMixin {

    @Inject(
            method = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            at = @At("HEAD")
    )
    private void pushLog(CallbackInfo ci) {
        //logCaller("pushPose");
    }

    @Inject(
            method = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V",
            at = @At("HEAD")
    )
    private void popLog(CallbackInfo ci) {
        //logCaller("popPose");
    }

    private static void logCaller(String method) {
        if (!MiapiItemModel.isRendering) {
            return;
        }
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stack) {
            String cls = element.getClassName();

            if (!cls.equals(Thread.class.getName())
                && !cls.equals(PoseStackMixin.class.getName())
                && !cls.equals(PoseStack.class.getName())
                && cls.contains("miapi")) {

                System.out.printf(
                        "[PoseStack] %s called from %s#%s(%s:%d)%n",
                        method,
                        cls,
                        element.getMethodName(),
                        element.getFileName(),
                        element.getLineNumber()
                );
                return;
            }
        }
    }
}