package smartin.miapi.mixin;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.item.modular.ModularItem;

import java.util.Iterator;
import java.util.List;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private ItemColors colors;

    /**
     * 10 bit Color Support, should be refactored to be less intrusive if possible
     */
    @Inject(method = "renderBakedItemQuads(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Ljava/util/List;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"), cancellable = true)
    private void colorSupport10bitColor(MatrixStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay, CallbackInfo ci){
        boolean bl = !stack.isEmpty();
        MatrixStack.Entry entry = matrices.peek();
        Iterator var9 = quads.iterator();

        while(var9.hasNext()) {
            BakedQuad bakedQuad = (BakedQuad)var9.next();
            int i = -1;
            if (bl && bakedQuad.hasColor()) {
                ItemColors colors = ((ItemRendererAccessor)this).color();
                i = colors.getColor(stack, bakedQuad.getColorIndex());
            }
            if(i>>30 == -2 && stack.getItem() instanceof ModularItem){
                float f = (float)(i >> 20 & 1023) / 255.0F;
                float g = (float)(i >> 10 & 1023) / 255.0F;
                float h = (float)(i & 1023) / 255.0F;
                vertices.quad(entry, bakedQuad, f, g, h, light, overlay);
            }
            else{
                float f = (float)(i >> 16 & 255) / 255.0F;
                float g = (float)(i >> 8 & 255) / 255.0F;
                float h = (float)(i & 255) / 255.0F;
                vertices.quad(entry, bakedQuad, f, g, h, light, overlay);
            }
        }
    }
}
