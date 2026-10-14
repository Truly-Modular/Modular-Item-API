package smartin.miapi.client.model.module.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.redpxnda.nucleus.util.Color;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.collision.CollisionHelper;
import smartin.miapi.client.model.collision.Ray;
import smartin.miapi.client.model.collision.RayHit;
import smartin.miapi.client.model.module.baked.passes.RenderPass;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.mixin.PoseAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.AlphaOverwriteProperty;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.List;
import java.util.Optional;

/**
 * high performance rendering of Baked Models with no overwrites.
 * this class is not meant to be interacted with from the outside!
 * use the {@link RenderPass#COLLECT_PASSES} to add/remove things to be rendered!
 */
@Environment(EnvType.CLIENT)
public class BakedMiapiModelNoOverrides implements MiapiModel {
    final RenderPass[] passes;
    final int skyLight;
    final int blockLight;
    final float[] colors;
    final float alpha;
    final RandomSource randomSource = RandomSource.create();
    final Matrix4f modelMatrix;
    final List<Pair<TextureAtlasSprite, List<BakedQuad>>> model;
    final int randomColor = FastColor.ARGB32.color(180, (int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));

    public BakedMiapiModelNoOverrides(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack, ItemDisplayContext context) {
        ModuleInstance instance = holder.colorProvider().adapt(moduleInstance);
        Color color = holder.colorProvider().getVertexColor().orElse(ColorProperty.getColor(stack, instance));
        this.colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        int[] propertyLight = EmissivityProperty.getLightValues(instance);
        skyLight = Math.max(holder.lightValues()[0], propertyLight[0]);
        blockLight = Math.max(holder.lightValues()[1], propertyLight[1]);
        this.modelMatrix = holder.matrix4f();

        alpha = AlphaOverwriteProperty.property
                .getData(moduleInstance)
                .map(DoubleOperationResolvable::getValue)
                .orElse(1.0d)
                .floatValue();
        passes = RenderPass.getRenderPasses(holder, moduleInstance, stack, context, new BakedModelCache(holder.model()), randomSource);
        this.model = ModelHelper.buildBatches(holder.model(), RandomSource.create());
    }

    @Override
    public void render(RenderContext context) {
        Minecraft.getInstance().getProfiler().push("BakedModel");
        context.matrices().pushPose();

        int sky = LightTexture.sky(context.light());
        int block = LightTexture.block(context.light());

        if (skyLight > sky) sky = skyLight;
        if (blockLight > block) block = blockLight;

        int light = LightTexture.pack(block, sky);

        context.matrices().mulPose(modelMatrix);
        RenderContext fuckForgeContext;
        if (Platform.isNeoForge() && context.matrices().last().normal().determinant() < 0.0001f) {
            PoseStack fuckForge = new PoseStack();
            fuckForge.last().pose().mul(context.matrices().last().pose());
            ((PoseAccessor) (Object) fuckForge.last()).miapi$callComputeNormalMatrix();
            fuckForge.last().normal().invert();
            fuckForgeContext = new RenderContext(
                    context.modelType(),
                    fuckForge,
                    context.stack(),
                    context.transformationMode(),
                    context.tickDelta(),
                    context.vertexConsumers(),
                    context.entity(),
                    context.light(),
                    context.overlay());
        } else {
            fuckForgeContext = context;
        }
        for (RenderPass pass : passes) {
            pass.render(fuckForgeContext, colors, alpha, light);
        }
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();
    }

    public Optional<RayHit> raycast(Ray ray, RenderContext context, boolean debug) {
        context.matrices().pushPose();
        Transform.applyPosition(context.matrices(), modelMatrix);
        if (debug && context.vertexConsumers() != null) {
            CollisionHelper.renderRedQuads(model, context.matrices().last().pose(), context.vertexConsumers(), randomColor);
        }
        Optional<RayHit> hit = CollisionHelper.raycast(ray, model, context.matrices().last().pose());
        context.matrices().popPose();
        return hit;
    }

}
