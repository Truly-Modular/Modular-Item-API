package smartin.miapi.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;

public class ModularRenderer extends BlockEntityWithoutLevelRenderer {
    public ModularRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        //
        //Lighting.setupFor3DItems();
        //Lighting.setupForFlatItems();
        //RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        //RenderSystem.enableBlend();
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.enableDepthTest();
        //Lighting.setupForFlatItems();
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();

        if (displayContext.equals(ItemDisplayContext.GUI)) {
            //Lighting.setupForFlatItems();
            //packedLight = 15728880;
            //test += 100;
            //packedLight = test;
            //packedLight = (int)Long.parseLong("0F0080",16);
            //packedOverlay = OverlayTexture.NO_OVERLAY;
            //poseStack.translate(0.0f, 0.0F, 0.0F);
            //poseStack.last().transformNormal(new Vector3f(-1, -1, -1), new Vector3f(0, -1, 0));
        }
        //Lighting.setupForFlatItems();
        MiapiModel model = MiapiItemModel.getItemModel(stack);
        if (model != null) {
            model.render(new MiapiModel.RenderContext(
                    null,
                    poseStack,
                    stack,
                    displayContext,
                    Minecraft.getInstance().getTimer().getRealtimeDeltaTicks(),
                    immediate,
                    Minecraft.getInstance().player,
                    packedLight,
                    packedOverlay
            ));
        }
        immediate.endBatch();
        if (buffer instanceof MultiBufferSource.BufferSource multiBufferSource) {
            //multiBufferSource.endBatch();
        }
        //Lighting.setupFor3DItems();
        //Lighting.setupFor3DItems();
    }
}
