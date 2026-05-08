package smartin.miapi.client.model.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import smartin.miapi.client.renderer.ObjectUVVertexConsumer;
import smartin.miapi.client.renderer.TrimRenderer;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.GlintProperty;
import smartin.miapi.modules.properties.render.AlphaOverwriteProperty;
import smartin.miapi.modules.properties.render.ColorProperty;
import smartin.miapi.modules.properties.render.EmissivityProperty;
import smartin.miapi.modules.properties.render.colorproviders.ColorProvider;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class BakedMiapiModelRework implements MiapiModel {
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
    boolean trimModel = !MiapiConfig.getClientConfig().render.enableFastTrim;
    private final Map<BakedModel, BakedModelCache> modelBatchCache = new IdentityHashMap<>();
    //to avoid excessive pair creation


    public BakedMiapiModelRework(ModelHolder holder, ModuleInstance moduleInstance, ItemStack stack) {
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

    @Override
    public void render(RenderContext context) {
        assert Minecraft.getInstance().level != null;
        Minecraft.getInstance().getProfiler().push("BakedModel");
        context.matrices().pushPose();

        int sky = LightTexture.sky(context.light());
        int block = LightTexture.block(context.light());

        if (skyLight > sky) sky = skyLight;
        if (blockLight > block) block = blockLight;

        int light = LightTexture.pack(block, sky);

        Transform.applyPosition(context.matrices(), modelMatrix);
        BakedModel currentModel = resolve(model, context.stack(), context.getEntitySave(), light);
        BakedModelCache cache = modelBatchCache.computeIfAbsent(currentModel, BakedModelCache::new);
        for (DoubleQuadCache batch : cache.getForward(modelHolder.colorProvider(), context.stack(), instance, context.transformationMode())) {
            batch.render(context.vertexConsumers(), context.matrices().last(), colors[0], colors[1], colors[2], alpha, light, context.overlay());
        }
        if (modelHolder.entityRendering()) {
            for (DoubleQuadCache batch : cache.getBackwards(modelHolder.colorProvider(), context.stack(), instance, context.transformationMode())) {
                batch.render(context.vertexConsumers(), context.matrices().last(), colors[0], colors[1], colors[2], alpha, light, context.overlay());
            }
        }

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
                for (DoubleQuadCache batch : cache.getForward(modelHolder.colorProvider(), context.stack(), instance, context.transformationMode())) {
                    for (BakedQuad quad : batch.original) {
                        Color glintColor = settings.getColor();
                        altConsumer.putBulkData(context.matrices().last(), quad,
                                glintColor.redAsFloat(),
                                glintColor.greenAsFloat(),
                                glintColor.blueAsFloat(),
                                glintColor.alphaAsFloat() * alpha * alphaAdjust, light, context.overlay());
                    }
                }
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("rendering glint error in module " + instance.moduleId() + " " + MaterialProperty.getMaterial(instance), e);
            }
        }


        if (trimModel) {
            Minecraft.getInstance().getProfiler().push("TrimModel");
            //render Trims
            Holder<ArmorMaterial> armorMaterial = (context.stack().getItem() instanceof ArmorItem armorItem) ? armorItem.getMaterial() : null;

            if (armorMaterial != null && !modelHolder.trimMode().equals(TrimRenderer.TrimMode.NONE)) {
                for(BakedQuad quad: cache.getTrim()){
                    TrimRenderer.renderTrims(context.matrices(), quad, modelHolder.trimMode(), light, context.vertexConsumers(), armorMaterial, context.stack());
                }
            }
            Minecraft.getInstance().getProfiler().pop();
        }
        context.matrices().popPose();
        Minecraft.getInstance().getProfiler().pop();
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

    class BakedModelCache {
        BakedModel model;
        DoubleQuadCache[] forward;
        DoubleQuadCache[] backwards;
        BakedQuad[] trim;


        public BakedModelCache(BakedModel model) {
            this.model = model;
        }

        public DoubleQuadCache[] getForward(ColorProvider colorProvider, ItemStack itemStack, ModuleInstance moduleInstance, ItemDisplayContext context) {
            if (forward == null) {
                forward = buildBatches(model, random, colorProvider, itemStack, moduleInstance, context);
            }
            return forward;
        }

        public DoubleQuadCache[] getBackwards(ColorProvider colorProvider, ItemStack itemStack, ModuleInstance moduleInstance, ItemDisplayContext context) {
            if (backwards == null) {
                DoubleQuadCache[] forwardArray = getForward(colorProvider, itemStack, moduleInstance, context);
                backwards = new DoubleQuadCache[forwardArray.length];
                for (int i = 0; i < forwardArray.length; i++) {
                    VertexConsumerProvider provider = new VertexConsumerProvider();
                    DoubleQuadCache cache = forward[i];
                    colorProvider.getConsumer(cache.sprite, itemStack, moduleInstance, context, provider);
                    backwards[i] = new DoubleQuadCache(ModelTransformer.getInverse(cache.original), provider, cache.sprite);
                }
            }
            return backwards;
        }

        public BakedQuad[] getTrim() {
            if (trim == null) {
                trim = ModelTransformer.getRescale(model, random).toArray(new BakedQuad[0]);
            }
            return trim;
        }
    }

    public static DoubleQuadCache[] buildBatches(BakedModel model, RandomSource random,
                                                 ColorProvider colorProvider,
                                                 ItemStack itemStack,
                                                 ModuleInstance moduleInstance,
                                                 ItemDisplayContext context) {
        Map<TextureAtlasSprite, List<BakedQuad>> temp = new HashMap<>();

        for (Direction dir : Direction.values()) {
            List<BakedQuad> quads = model.getQuads(null, dir, random);

            for (BakedQuad quad : quads) {
                temp.computeIfAbsent(quad.getSprite(), s -> new ArrayList<>())
                        .add(quad);
            }
        }

        DoubleQuadCache[] result = new DoubleQuadCache[temp.size()];
        int i = 0;
        for (Map.Entry<TextureAtlasSprite, List<BakedQuad>> entry : temp.entrySet()) {
            VertexConsumerProvider provider = new VertexConsumerProvider();
            colorProvider.getConsumer(entry.getKey(), itemStack, moduleInstance, context, provider);
            List<BakedQuad> quads = entry.getValue();
            result[i] = new DoubleQuadCache(quads.toArray(new BakedQuad[quads.size()]), provider, entry.getKey());
            i++;
        }
        return result;
    }


    public static class DoubleQuadCache {
        TextureAtlasSprite sprite;
        BakedQuad[] original;
        VertexConsumerProvider vcProvider;
        BakedQuad[] moved;

        public DoubleQuadCache(BakedQuad[] forward, VertexConsumerProvider vertexConsumerProvider, TextureAtlasSprite sprite) {
            this.original = forward;
            this.vcProvider = vertexConsumerProvider;
            this.sprite = sprite;
        }

        public void render(MultiBufferSource bufferSource, PoseStack.Pose pose, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
            if (vcProvider.isMovedBlockAtlasValid()) {
                Minecraft.getInstance().getProfiler().push("vc");
                if (moved == null) {
                    moved = ModelTransformer.getOffset(original, vcProvider.u, vcProvider.v);
                }
                vcProvider.spriteSlot.used = 3;
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
}
