package smartin.miapi.client.model.module.dynamic.trail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.module.dynamic.ChainModel;
import smartin.miapi.client.model.module.dynamic.DynamicModel;
import smartin.miapi.client.model.module.dynamic.MatrixHelper;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;

import java.util.Iterator;
import java.util.List;

public class DynamicTrailModel extends DynamicModel<TrailState> {

    private final int maxPoints;
    private final float pointLifetime;
    private final float sampleInterval;
    private final float thickness;
    private final Color color;
    private final Transform transform;
    private final TextureAtlasSprite texture;
    public final ColorProvider colorProvider;
    public final ModuleInstance moduleInstance;
    private final boolean debug;
    private final List<ItemDisplayContext> whiteList;

    public DynamicTrailModel(
            int maxPoints,
            float pointLifetime,
            float sampleInterval,
            float thickness,
            Color color,
            boolean debug,
            ResourceLocation texture,
            Transform transform,
            ColorProvider colorProvider,
            ModuleInstance moduleInstance,
            List<ItemDisplayContext> whiteList
    ) {
        this.maxPoints = maxPoints;
        this.pointLifetime = pointLifetime;
        this.sampleInterval = sampleInterval;
        this.thickness = thickness;
        this.color = color;
        this.transform = transform;
        this.texture = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        this.colorProvider = colorProvider;
        this.moduleInstance = moduleInstance;
        this.debug = debug;
        this.whiteList = whiteList;
    }

    @Override
    protected TrailState createState(Vec3 pos, RenderContext context) {
        return new TrailState();
    }

    @Override
    public void updatePhysics(
            TrailState state,
            @Nullable LivingEntity entity,
            float delta,
            PoseStack pose,
            Vector3f down,
            ItemDisplayContext context
    ) {
        if (!whiteList.contains(context)) {
            return;
        }
        pose.pushPose();
        pose.mulPose(transform.toMatrix());
        float currentTime = (float) MiapiClient.currentTickFull();

        Iterator<TrailState.TrailPoint> it = state.points.iterator();
        while (it.hasNext()) {
            TrailState.TrailPoint p = it.next();
            p.age += delta;
            if (p.age > pointLifetime) {
                it.remove();
            }
        }
        if (currentTime - state.lastSpawnTime < sampleInterval) {
            pose.popPose();
            return;
        }
        state.lastSpawnTime = currentTime;

        Vector3f localOrigin = new Vector3f(0, 0, 0);
        Vector3f worldPos = MatrixHelper.translatePositionToWorldSpace(
                pose,
                new Vector3f(localOrigin).sub(0, thickness, 0),
                state.cameraPose
        );
        Vector3f worldPos2 = MatrixHelper.translatePositionToWorldSpace(
                pose,
                new Vector3f(localOrigin).sub(0, -thickness, 0),
                state.cameraPose
        );

        state.points.addLast(new TrailState.TrailPoint(
                worldPos,
                worldPos2
        ));

        while (state.points.size() > maxPoints) {
            state.points.removeFirst();
        }
        pose.popPose();
    }


    @Override
    protected Matrix4f computeEnd(TrailState state, PoseStack poseStack) {
        return new Matrix4f();
    }

    @Override
    protected void renderDynamic(
            RenderContext context,
            Vector3f start,
            Matrix4f end,
            TrailState state
    ) {
        if (state.points.size() < 2) return;

        PoseStack poseStack = context.matrices();
        poseStack.pushPose();
        poseStack.mulPose(transform.toMatrix());

        Vec3 camPos = state.cameraPose;


        VertexConsumer vc = colorProvider.getConsumer(context.vertexConsumers(), texture, context.stack(), moduleInstance, context.transformationMode());
        /*
        VertexConsumer vc = ItemRenderer.getFoilBufferDirect(
                context.vertexConsumers(),
                ItemBlockRenderTypes.getRenderType(ItemStack.EMPTY, false),
                true,
                context.stack().hasFoil()
        );

         */

        TrailState.TrailPoint prev = new TrailState.TrailPoint(MatrixHelper.translatePositionToWorldSpace(
                poseStack,
                new Vector3f().sub(0, thickness, 0),
                state.cameraPose
        ), MatrixHelper.translatePositionToWorldSpace(
                poseStack,
                new Vector3f().sub(0, -thickness, 0),
                state.cameraPose
        ));

        for (TrailState.TrailPoint point : state.points.reversed()) {
            float alpha = 1f - (point.age / pointLifetime);
            float alpha2 = 1f - (prev.age / pointLifetime);

            Vector3f a = MatrixHelper.translatePositionToLocalSpace(poseStack, prev.worldPos, camPos);

            Vector3f b = MatrixHelper.translatePositionToLocalSpace(poseStack, prev.worldPos2, camPos);
            Vector3f c = MatrixHelper.translatePositionToLocalSpace(poseStack, point.worldPos2, camPos);
            Vector3f d = MatrixHelper.translatePositionToLocalSpace(poseStack, point.worldPos, camPos);

            if (debug) {
                ChainModel.drawLine(poseStack, context.vertexConsumers(), a, b, 1f, 0f, 0f, 1.0f);
                ChainModel.drawLine(poseStack, context.vertexConsumers(), b, c, 0f, 1f, 0f, 1.0f);
                ChainModel.drawLine(poseStack, context.vertexConsumers(), c, d, 0f, 0f, 1f, 1.0f);
                ChainModel.drawLine(poseStack, context.vertexConsumers(), d, a, 1f, 1f, 1f, 1.0f);
                vc = colorProvider.getConsumer(context.vertexConsumers(), texture, context.stack(), moduleInstance, context.transformationMode());
            }

            submitVertex(vc, poseStack, a, alpha2, context, 0, 0);
            submitVertex(vc, poseStack, b, alpha2, context, 1, 0);
            submitVertex(vc, poseStack, c, alpha, context, 1, 1);
            submitVertex(vc, poseStack, d, alpha, context, 0, 1);

            submitVertex(vc, poseStack, c, alpha, context, 1, 1);
            submitVertex(vc, poseStack, b, alpha2, context, 1, 0);
            submitVertex(vc, poseStack, a, alpha2, context, 0, 0);
            submitVertex(vc, poseStack, d, alpha, context, 0, 1);
            prev = point;
        }

        poseStack.popPose();
    }

    private void submitVertex(
            VertexConsumer vc,
            PoseStack poseStack,
            Vector3f pos,
            float alpha,
            RenderContext context,
            float u,
            float v
    ) {
        float uu = u == 0 ? texture.getU0() : texture.getU1();
        float vv = v == 0 ? texture.getV0() : texture.getV1();
        Vector3f actualPos = poseStack.last().pose().transformPosition(pos, new Vector3f());
        vc.addVertex(actualPos.x, actualPos.y, actualPos.z, color.withAlpha(alpha * color.alphaAsFloat()).abgr(), uu, vv, context.overlay(), context.light(), 0, 0, -1);
    }
}