package smartin.miapi.client.model.module.baked;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.module.baked.passes.RenderPass;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.AlphaOverwriteProperty;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.IdentityHashMap;

@Environment(EnvType.CLIENT)
public class BakedMiapiModelWithOverrides implements MiapiModel {
    final IdentityHashMap<BakedModel, RenderPass[]> cache = new IdentityHashMap<>();
    final int skyLight;
    final int blockLight;
    final float[] colors;
    final float alpha;
    final RandomSource randomSource = RandomSource.create();
    final Matrix4f modelMatrix;
    final ModelHolder holder;
    final ModuleInstance instance;

    public BakedMiapiModelWithOverrides(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack, ItemDisplayContext context) {
        this.instance = holder.colorProvider().adapt(moduleInstance);
        this.holder = holder;
        Color color = holder.colorProvider().getVertexColor().orElse(ColorProperty.getColor(stack, instance));
        this.colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        int[] propertyLight = EmissivityProperty.getLightValues(instance);
        skyLight = Math.max(holder.lightValues()[0], propertyLight[0]);
        blockLight = Math.max(holder.lightValues()[1], propertyLight[1]);
        this.modelMatrix = holder.matrix4f();
        alpha = AlphaOverwriteProperty.property
                .getData(instance)
                .map(DoubleOperationResolvable::getValue)
                .orElse(1.0d)
                .floatValue();
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

        Transform.applyPosition(context.matrices(), modelMatrix);
        BakedModel currentModel = resolve(holder.model(), context.stack(), context.getEntitySave(), light);
        RenderPass[] passes = cache.computeIfAbsent(currentModel, m ->
                RenderPass.getRenderPasses(holder, instance, context.stack(), context.transformationMode(), new BakedModelCache(m), randomSource));
        for (RenderPass pass : passes) {
            pass.render(context, colors, alpha, light);
        }
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();
    }

    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable LivingEntity entity, int light) {
        BakedModel override = model.getOverrides().resolve(model, stack, Minecraft.getInstance().level, entity, light);
        if (model != null) {
            model = override;
        }
        return model;
    }
}
