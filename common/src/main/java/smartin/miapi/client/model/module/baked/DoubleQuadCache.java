package smartin.miapi.client.model.module.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.atlas.VertexConsumerProvider;

public class DoubleQuadCache {
    public TextureAtlasSprite sprite;
    public BakedQuad[] original;
    public VertexConsumerProvider vcProvider;
    public BakedQuad[] moved;

    public DoubleQuadCache(BakedQuad[] forward, VertexConsumerProvider vertexConsumerProvider, TextureAtlasSprite sprite) {
        this.original = forward;
        this.vcProvider = vertexConsumerProvider;
        this.sprite = sprite;
    }

    public void render(MultiBufferSource bufferSource, PoseStack.Pose pose, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
        if (false) {
            //this optimation would require rebuild on SpriteSlot invalidation
            //this is a bit impractical and prob not worth the work
            Minecraft.getInstance().getProfiler().push("vc");
            if (moved == null) {
                //moved = ModelTransformer.getOffset(original, vcProvider.u, vcProvider.v);
            }
            if (vcProvider.spriteSlot.used < 3) {
                vcProvider.spriteSlot.used = 3;
            }
            VertexConsumer vc = MaterialSpriteManager.getVanillaItemVC(bufferSource);
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().push("quads");
            for (BakedQuad quad : moved) {
                vc.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
            }
            Minecraft.getInstance().getProfiler().pop();
        } else {
            Minecraft.getInstance().getProfiler().push("vc");
            VertexConsumer vc = vcProvider.getRenderSaveVC.apply(bufferSource);
            Minecraft.getInstance().getProfiler().pop();
            Minecraft.getInstance().getProfiler().push("quads");
            for (BakedQuad bakedQuad : original) {
                vc.putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay);
            }
            Minecraft.getInstance().getProfiler().pop();
        }
    }
}
