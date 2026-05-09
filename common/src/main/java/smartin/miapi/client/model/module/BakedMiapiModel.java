package smartin.miapi.client.model.module;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.Miapi;
import smartin.miapi.client.GlintShader;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.atlas.MaterialSpriteManager;
import smartin.miapi.client.atlas.VertexConsumerProvider;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModelHolder;
import smartin.miapi.client.model.ModelTransformer;
import smartin.miapi.client.model.item.ItemBakedModelOverrides;
import smartin.miapi.client.model.module.baked.BakedMiapiModelNoOverrides;
import smartin.miapi.client.model.module.baked.BakedMiapiModelWithOverrides;
import smartin.miapi.client.renderer.ObjectUVVertexConsumer;
import smartin.miapi.client.renderer.RescaledVertexConsumer;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.mixin.client.BufferBuilderAccessor;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.modules.properties.render.AlphaOverwriteProperty;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BakedMiapiModel implements MiapiModel {
    ModuleInstance instance;
    BakedModel model;
    Matrix4f modelMatrix;
    ModelHolder modelHolder;
    RandomSource random = RandomSource.create();
    float[] colors;
    GlintProperty.GlintSettings settings;
    int skyLight;
    int blockLight;
    float alpha;
    static VertexConsumer lastVC;
    static ColorProvider lastColor;
    static TextureAtlasSprite textureAtlasSprite;
    boolean trimModel = !MiapiConfig.getClientConfig().render.enableFastTrim;
    private final Map<BakedModel, List<Pair<TextureAtlasSprite, List<BakedQuad>>>> modelBatchCache = new IdentityHashMap<>();

    public static MiapiModel createBaked(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack, ItemDisplayContext context) {
        if (MiapiConfig.getClientConfig().render.optimisedModel) {
            if (holder.model().getOverrides() != null && !holder.model().getOverrides().equals(ItemBakedModelOverrides.EMPTY)) {
                return new BakedMiapiModelWithOverrides(holder, moduleInstance, stack, context);
            } else {
                return new BakedMiapiModelNoOverrides(holder, moduleInstance, stack, context);
            }
        }
        return new BakedMiapiModel(holder, moduleInstance, stack);
    }


    public BakedMiapiModel(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack) {
        this.modelHolder = holder;
        this.instance = holder.colorProvider().adapt(moduleInstance);
        Color color = holder.colorProvider().getVertexColor().orElse(ColorProperty.getColor(stack, instance));
        this.colors = new float[]{color.redAsFloat(), color.greenAsFloat(), color.blueAsFloat()};
        this.modelMatrix = holder.matrix4f();
        this.model = holder.model();
        settings = GlintProperty.property.getGlintSettings(instance, stack);

        skyLight = holder.lightValues()[0];
        blockLight = holder.lightValues()[1];

        int[] propertyLight = EmissivityProperty.getLightValues(instance);
        int propertySky = propertyLight[0];
        int propertyBlock = propertyLight[1];

        alpha = AlphaOverwriteProperty.property
                .getData(moduleInstance)
                .map(DoubleOperationResolvable::getValue)
                .orElse(1.0d)
                .floatValue();

        if (propertySky > skyLight) skyLight = propertySky;
        if (propertyBlock > blockLight) blockLight = propertyBlock;
    }

    private List<Pair<TextureAtlasSprite, List<BakedQuad>>> buildBatches(BakedModel model, RandomSource random) {
        Map<TextureAtlasSprite, List<BakedQuad>> temp = new HashMap<>();

        for (Direction dir : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(null, dir, random);

            for (BakedQuad quad : quads) {
                temp.computeIfAbsent(quad.getSprite(), s -> new ArrayList<>())
                        .add(quad);
            }
        }

        List<Pair<TextureAtlasSprite, List<BakedQuad>>> result = new ArrayList<>(temp.size());

        for (Map.Entry<TextureAtlasSprite, List<BakedQuad>> entry : temp.entrySet()) {
            result.add(Pair.of(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    @Override
    public void render(RenderContext context) {
        assert Minecraft.getInstance().level != null;
        Map<TextureAtlasSprite, VertexConsumer> lookup = new HashMap<>();
        Minecraft.getInstance().getProfiler().push("BakedModel");
        Minecraft.getInstance().getProfiler().push("BakedModel-logic");
        context.matrices().pushPose();

        int sky = LightTexture.sky(context.light());
        int block = LightTexture.block(context.light());

        if (skyLight > sky) sky = skyLight;
        if (blockLight > block) block = blockLight;

        int light = LightTexture.pack(block, sky);

        Transform.applyPosition(context.matrices(), modelMatrix);
        BakedModel currentModel = resolve(model, context.stack(), context.getEntitySave(), light);
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().push("BakedModel - quads");
        try {
            List<Pair<TextureAtlasSprite, List<BakedQuad>>> batches =
                    modelBatchCache.computeIfAbsent(currentModel, m ->
                            buildBatches(m, random)
                    );

            for (Pair<TextureAtlasSprite, List<BakedQuad>> batch : batches) {
                TextureAtlasSprite sprite = batch.getFirst();
                List<BakedQuad> quads = batch.getSecond();

                VertexConsumer consumer = getConsumer(
                        modelHolder.colorProvider(),
                        sprite,
                        context.vertexConsumers(),
                        context.stack(),
                        instance,
                        context.transformationMode()
                );

                for (BakedQuad quad : quads) {
                    consumer.putBulkData(
                            context.matrices().last(),
                            quad,
                            colors[0], colors[1], colors[2],
                            alpha,
                            light,
                            context.overlay()
                    );
                }
            }
        } catch (RuntimeException e) {
            Miapi.LOGGER.error(
                    "rendering error in module " + instance.moduleId() + " " +
                    MaterialProperty.getMaterial(instance),
                    e
            );
            MaterialSpriteManager.clear();
        }

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().push("BakedModel Glint");

        //render normally
        if (context.stack().hasFoil() && MiapiConfig.getClientConfig().enchantingGlint.shouldRenderGlint()) {
            try {
                VertexConsumer altConsumer;
                if (MiapiClient.CUSTOM_SHADER_LOADED) {
                    altConsumer = new ObjectUVVertexConsumer(
                            context.vertexConsumers().getBuffer(GlintShader.modularItemGlint),
                            context.objectSpace(), true, 1.0f
                    );
                } else {
                    altConsumer = new ObjectUVVertexConsumer(
                            context.vertexConsumers().getBuffer(RenderType.entityGlintDirect()),
                            context.objectSpace(), false, 1.0f
                    );
                }
                float alphaAdjust;
                if (
                        context.transformationMode() == ItemDisplayContext.HEAD
                    //context.modelType() != null &&
                    //!("item".equals(context.modelType()))
                ) {
                    alphaAdjust = MiapiConfig.getClientConfig().enchantingGlint.armorEnchantmentAlphaAdjust;
                } else {
                    alphaAdjust = 1.0f;
                }
                for (Direction dir : Direction.values()) {
                    currentModel.getQuads(null, dir, RandomSource.create()).forEach(quad -> {
                        Color glintColor = settings.getColor();
                        altConsumer.putBulkData(context.matrices().last(), quad,
                                glintColor.redAsFloat(),
                                glintColor.greenAsFloat(),
                                glintColor.blueAsFloat(),
                                glintColor.alphaAsFloat() * alpha * alphaAdjust, light, context.overlay());

                    });
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("rendering glint error in module " + instance.moduleId() + " " + MaterialProperty.getMaterial(instance), e);
            }
        }
        Minecraft.getInstance().getProfiler().pop();


        if (trimModel) {
            Minecraft.getInstance().getProfiler().push("TrimModel");
            //render Trims
            Holder<ArmorMaterial> armorMaterial = (context.stack().getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : null;

            if (armorMaterial != null && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
                ModelTransformer.getRescale(currentModel, random).forEach(quad -> {
                    TrimRenderer.renderTrims(context.matrices(), quad, modelHolder.trimMode(), light, context.vertexConsumers(), armorMaterial, context.stack());
                });
            }
            Minecraft.getInstance().getProfiler().pop();
        }

        lookup.clear();
        //render from both sides if requested
        if (modelHolder.entityRendering()) {
            Minecraft.getInstance().getProfiler().push("EntityModel");
            ModelTransformer.getInverse(currentModel, random).forEach(quad -> {
                VertexConsumer vertexConsumer = getConsumer(
                        modelHolder.colorProvider(),
                        quad.getSprite(),
                        context.vertexConsumers(), context.stack(),
                        instance, context.transformationMode());
                vertexConsumer.putBulkData(
                        context.matrices().last(), quad,
                        colors[0], colors[1], colors[2],
                        alpha, light, context.overlay());
            });
            Minecraft.getInstance().getProfiler().pop();
        }
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();
    }

    public boolean isStillValid(VertexConsumer vertexConsumer) {
        if (vertexConsumer instanceof RescaledVertexConsumer rescaledVertexConsumer) {
            return isStillValid(rescaledVertexConsumer.delegate);
        } else if (vertexConsumer instanceof BufferBuilder buffer) {
            return ((BufferBuilderAccessor) buffer).isBuilding();
        }
        return false;
    }

    VertexConsumerProvider Vcprovider = new VertexConsumerProvider();

    public VertexConsumer getConsumer(ColorProvider provider, TextureAtlasSprite sprite, MultiBufferSource source, ItemStack itemStack, ModuleInstance instance, ItemDisplayContext context) {
        if (provider.equals(lastColor) && sprite.equals(textureAtlasSprite) && isStillValid(lastVC)) {
            return lastVC;
        }
        Minecraft.getInstance().getProfiler().push("Building VC");
        provider.getConsumer(sprite, itemStack, instance, context, Vcprovider);
        lastVC = Vcprovider.getRenderSaveVC.apply(source);
        Minecraft.getInstance().getProfiler().pop();
        lastColor = provider;
        textureAtlasSprite = sprite;
        return lastVC;
    }

    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable LivingEntity entity, int light) {
        if (model.getOverrides() != null && !model.getOverrides().equals(ItemOverrides.EMPTY)) {
            BakedModel override = model.getOverrides().resolve(model, stack, Minecraft.getInstance().level, entity, light);
            if (model != null) {
                model = override;
            }
        }
        return model;
    }
}
