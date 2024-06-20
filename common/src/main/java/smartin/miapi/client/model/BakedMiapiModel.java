package smartin.miapi.client.model;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.EmissiveProperty;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.registries.RegistryInventory;

public class BakedMiapiModel implements MiapiModel {
    ItemModule.ModuleInstance instance;
    BakedModel model;
    Matrix4f modelMatrix;
    ModelHolder modelHolder;
    Random random = Random.create();
    float[] colors;
    GlintProperty.GlintSettings settings;
    int skyLight;
    int blockLight;


    public BakedMiapiModel(ModelHolder holder, ItemModule.ModuleInstance moduleInstance, ItemStack stack) {
        this.modelHolder = holder;
        this.instance = moduleInstance;
        Color color = holder.colorProvider().getVertexColor();
        this.colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        this.modelMatrix = holder.matrix4f();
        this.model = holder.model();
        settings = GlintProperty.property.getGlintSettings(moduleInstance, stack);

        if (holder.lightValues() != null) {
            skyLight = holder.lightValues()[0];
            blockLight = holder.lightValues()[1];
        } else {
            skyLight = -1;
            blockLight = -1;
        }

        int[] propertyLight = EmissiveProperty.getLightValues(moduleInstance);
        int propertySky = propertyLight[0];
        int propertyBlock = propertyLight[1];

        if (propertySky > skyLight) skyLight = propertySky;
        if (propertyBlock > blockLight) blockLight = propertyBlock;
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode transformationMode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int packedLight, int overlay) {
        assert MinecraftClient.getInstance().world != null;
        matrices.push();

        int sky = LightmapTextureManager.getSkyLightCoordinates(packedLight);
        int block = LightmapTextureManager.getBlockLightCoordinates(packedLight);

        if (skyLight != -1) sky = skyLight;
        if (blockLight != -1) block = blockLight;

        int light = LightmapTextureManager.pack(block, sky);

        Transform.applyPosition(matrices, modelMatrix);
        BakedModel currentModel = resolve(model, stack, entity, light);
        MinecraftClient.getInstance().world.getProfiler().push("BakedModel");

        //render normally
        for (Direction dir : Direction.values()) {
            currentModel.getQuads(null, dir, Random.create()).forEach(quad -> {
                VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);
                vertexConsumer.quad(matrices.peek(), quad, colors[0], colors[1], colors[2], light, overlay);
                if (stack.hasGlint()) {
                    VertexConsumer altConsumer = vertexConsumers.getBuffer(RegistryInventory.Client.modularItemGlint);
                    Color glintColor = settings.getColor();
                    altConsumer.quad(matrices.peek(), quad, glintColor.redAsFloat(), glintColor.greenAsFloat(), glintColor.blueAsFloat(), light, overlay);
                }
            });
        }
        MinecraftClient.getInstance().world.getProfiler().pop();

        MinecraftClient.getInstance().world.getProfiler().push("TrimModel");
        //render Trims
        ArmorTrim trim = ArmorTrim.getTrim(entity.getWorld().getRegistryManager(), stack).orElse(null);
        ArmorMaterial armorMaterial = (stack.getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : null;

        if (trim != null && armorMaterial != null && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
            ModelTransformer.getRescale(currentModel, random).forEach(quad -> {
                TrimRenderer.renderTrims(matrices, quad, modelHolder.trimMode(), light, vertexConsumers, armorMaterial, stack);
            });
        }
        MinecraftClient.getInstance().world.getProfiler().pop();


        MinecraftClient.getInstance().world.getProfiler().push("EntityModel");
        //render from both sides if requested
        if (modelHolder.entityRendering()) {
            ModelTransformer.getInverse(currentModel, random).forEach(quad -> {
                VertexConsumer vertexConsumer = modelHolder.colorProvider().getConsumer(vertexConsumers, quad.getSprite(), stack, instance, transformationMode);
                vertexConsumer.quad(matrices.peek(), quad, colors[0], colors[1], colors[2], light, overlay);
                if (stack.hasGlint()) {
                    VertexConsumer altConsumer = vertexConsumers.getBuffer(RegistryInventory.Client.modularItemGlint);
                    Color glintColor = settings.getColor();
                    altConsumer.quad(matrices.peek(), quad, glintColor.redAsFloat(), glintColor.greenAsFloat(), glintColor.blueAsFloat(), light, overlay);
                }
            });
        }

        MinecraftClient.getInstance().world.getProfiler().pop();
        matrices.pop();
    }

    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable LivingEntity entity, int light) {
        if (model.getOverrides() != null && !model.getOverrides().equals(ModelOverrideList.EMPTY)) {
            BakedModel override = model.getOverrides().apply(model, stack, MinecraftClient.getInstance().world, entity, light);
            if (model != null) {
                model = override;
            }
        }
        return model;
    }
}
