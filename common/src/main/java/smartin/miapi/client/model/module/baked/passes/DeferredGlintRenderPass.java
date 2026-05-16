package smartin.miapi.client.model.module.baked.passes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import smartin.miapi.client.GlintShader;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.BakedModelCache;
import smartin.miapi.client.model.module.baked.DoubleQuadCache;
import smartin.miapi.client.renderer.ObjectUVVertexConsumer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;

import java.util.ArrayList;
import java.util.List;

public class DeferredGlintRenderPass implements RenderPass {
    private static final List<GlintCommand> COMMANDS = new ArrayList<>();

    DoubleQuadCache[] quadCaches;
    GlintProperty.GlintSettings settings;
    float alphaAdjust;

    public DeferredGlintRenderPass(
            ModuleInstance moduleInstance,
            ItemStack stack,
            GlintProperty.GlintSettings settings,
            ItemDisplayContext context,
            ModelHolder modelHolder,
            BakedModelCache cache,
            RandomSource random
    ) {
        quadCaches = cache.getForward(
                modelHolder.colorProvider(),
                stack,
                moduleInstance,
                context,
                random
        );
        this.settings = settings;
        alphaAdjust =
                context == ItemDisplayContext.HEAD
                        ? MiapiConfig.getClientConfig()
                        .enchantingGlint
                        .armorEnchantmentAlphaAdjust
                        : 1.0f;
    }

    public static void flush(MultiBufferSource buffers) {
        VertexConsumer consumer =
                buffers.getBuffer(GlintShader.modularItemGlint);
        PoseStack stack = new PoseStack();
        for (GlintCommand command : COMMANDS) {
            VertexConsumer glintConsumer = new ObjectUVVertexConsumer(
                    consumer,
                    command.objectSpace, true, 1.0f
            );
            stack.last().pose().set(command.pose());
            stack.last().normal().set(command.normal());
            for (DoubleQuadCache batch : command.quadCaches()) {
                for (BakedQuad quad : batch.original) {
                    glintConsumer.putBulkData(
                            stack.last(),
                            quad,
                            command.color().redAsFloat(),
                            command.color().greenAsFloat(),
                            command.color().blueAsFloat(),
                            command.color().alphaAsFloat() * command.alpha(),
                            command.light(),
                            command.overlay()
                    );
                }
            }
        }
        COMMANDS.clear();
    }

    @Override
    public void render(
            MiapiModel.RenderContext context,
            float[] colors,
            float alpha,
            int light
    ) {
        if (!context.glint()) return;
        PoseStack.Pose pose = context.matrices().last();
        COMMANDS.add(new GlintCommand(
                quadCaches,
                new Matrix4f(pose.pose()),
                new Matrix3f(pose.normal()),
                context.objectSpace(),
                settings.getColor(),
                alpha * alphaAdjust,
                light,
                context.overlay()
        ));
    }

    public record GlintCommand(
            DoubleQuadCache[] quadCaches,
            Matrix4f pose,
            Matrix3f normal,
            Matrix4f objectSpace,
            Color color,
            float alpha,
            int light,
            int overlay
    ) {
    }
}