package smartin.miapi.fabric.mixin.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.redpxnda.nucleus.impl.fabric.ShaderRegistryImpl;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ShaderRegistryImpl.class)
public class ShaderRegistryNucleusMixin {
    @Inject(method = "register(Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;Ljava/util/function/Consumer;)V", at = @At(value = "HEAD"))
    private static void miapi$fixShader(ResourceLocation loc, VertexFormat vertexFormat, Consumer<ShaderInstance> onLoad, CallbackInfo ci) {
        ShaderRegistryImpl.SHADERS.add(Triple.of(loc, vertexFormat, onLoad));
    }
}
