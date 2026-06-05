package smartin.miapi.client.model.module.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

/**
 * Adapter: MiAPI RenderContext -> GeckoLib GeoRenderer pipeline
 */
public class MiapiGeoRenderer<T extends GeoAnimatable> implements GeoRenderer<T> {

    private final GeoModel<T> model;
    private T animatable;

    public MiapiGeoRenderer(GeoModel<T> model) {
        this.model = model;
    }

    public void setAnimatable(T animatable) {
        this.animatable = animatable;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    @Override
    public T getAnimatable() {
        return animatable;
    }

    @Override
    public void fireCompileRenderLayersEvent() {
        
    }

    @Override
    public boolean firePreRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        return false;
    }

    @Override
    public void firePostRenderEvent(PoseStack poseStack, BakedGeoModel model, MultiBufferSource bufferSource, float partialTick, int packedLight) {

    }

    @Override
    public void updateAnimatedTextureFrame(T animatable) {

    }

    /**
     * Main entry from MiAPI
     */
    public void renderItem(MiapiContextBridge<T> ctx, T animatable) {
        this.animatable = animatable;

        defaultRender(
                ctx.poseStack(),
                animatable,
                ctx.bufferSource(),
                null,
                null,
                ctx.yaw(),
                ctx.partialTick(),
                ctx.light()
        );
    }

    /**
     * Optional bridge wrapper (maps your RenderContext)
     */
    public record MiapiContextBridge<T>(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ItemStack stack,
            float partialTick,
            int light,
            float yaw
    ) {}
}