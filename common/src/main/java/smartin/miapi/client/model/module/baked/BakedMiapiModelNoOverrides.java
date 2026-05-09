package smartin.miapi.client.model.module.baked;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.passes.RenderPass;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.AlphaOverwriteProperty;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

@Environment(EnvType.CLIENT)
public class BakedMiapiModelNoOverrides implements MiapiModel {
    final RenderPass[] passes;
    final int skyLight;
    final int blockLight;
    final float[] colors;
    final float alpha;
    final RandomSource randomSource = RandomSource.create();
    final Matrix4f modelMatrix;

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
        for (RenderPass pass : passes) {
            pass.render(context, colors, alpha, light);
        }
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();

    }
}
