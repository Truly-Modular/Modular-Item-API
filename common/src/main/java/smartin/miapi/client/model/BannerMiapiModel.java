package smartin.miapi.client.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import smartin.miapi.config.MiapiConfig;

import java.util.List;

public class BannerMiapiModel implements MiapiModel {
    ModelPart banner;
    List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns;
    BannerMode mode;
    Matrix4f transform;

    public BannerMiapiModel(List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns, BannerMode mode, Matrix4f transform) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("flag", ModelPartBuilder.create().uv(0, 0).cuboid(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), ModelTransform.NONE);
        this.banner = modelPartData.getChild("flag").createPart(64, 64);
        this.patterns = patterns;
        this.mode = mode;
        this.transform = transform;
    }

    public BannerMiapiModel(List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns, BakedModel model, Matrix4f transform) {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("flag", ModelPartBuilder.create().uv(0, 0).cuboid(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), ModelTransform.NONE);
        this.banner = modelPartData.getChild("flag").createPart(64, 64);
        this.patterns = patterns;
        this.transform = transform;
    }

    @Nullable
    public static BannerMiapiModel getFromStack(ItemStack stack, @Nullable BannerMode mode, Matrix4f transform, @Nullable BakedModel model) {
        if (stack.getItem() instanceof BannerItem bannerItem) {
            DyeColor dyeColor = bannerItem.getColor();
            NbtList patternList = BannerBlockEntity.getPatternListNbt(stack);
            if (dyeColor != null && patternList != null) {
                if (model != null) {
                    return new BannerMiapiModel(BannerBlockEntity.getPatternsFromNbt(dyeColor, patternList), model, transform);
                }
                if (mode == null) {
                    mode = BannerMode.MODEL;
                }
                return new BannerMiapiModel(BannerBlockEntity.getPatternsFromNbt(dyeColor, patternList), mode, transform);
            }
        }
        return null;
    }

    public static List<Pair<RegistryEntry<BannerPattern>, DyeColor>> getPatterns(ItemStack banner){
        if (banner.getItem() instanceof BannerItem bannerItem) {
            DyeColor dyeColor = bannerItem.getColor();
            NbtList patternList = BannerBlockEntity.getPatternListNbt(banner);
            if (dyeColor != null && patternList != null) {

                return BannerBlockEntity.getPatternsFromNbt(dyeColor, patternList);
            }
        }
        return null;
    }


    @Override
    public void render(MatrixStack matrices,
                       ItemStack stack,
                       ModelTransformationMode transformationMode,
                       float tickDelta,
                       VertexConsumerProvider vertexConsumers,
                       LivingEntity entity, int light, int overlay) {
        matrices.push();
        switch (mode) {
            case ITEM -> {
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.push();
                matrices.translate(8, -8, -8.75);
                matrices.scale(1, 1, -1);
                matrices.multiplyPositionMatrix(transform);
                matrices.peek().getNormalMatrix().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, banner, ModelLoader.BANNER_BASE, true, patterns, stack.hasGlint() && MiapiConfig.INSTANCE.client.other.enchantingGlint);
                matrices.pop();

                matrices.push();
                matrices.translate(8, -8, -7.25);
                matrices.scale(1, 1, 1);
                matrices.multiplyPositionMatrix(transform);
                matrices.peek().getNormalMatrix().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, banner, ModelLoader.BANNER_BASE, true, patterns, stack.hasGlint() && MiapiConfig.INSTANCE.client.other.enchantingGlint);
                matrices.pop();
            }
            case ITEM_ALT -> {
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.push();
                matrices.translate(8, -8, -8.75);
                matrices.scale(1, 1, -1);
                matrices.multiplyPositionMatrix(transform);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, banner, ModelLoader.BANNER_BASE, true, patterns, stack.hasGlint() && MiapiConfig.INSTANCE.client.other.enchantingGlint);
                matrices.pop();

                matrices.push();
                matrices.translate(8, -8, -7.25);
                matrices.scale(1, 1, 1);
                matrices.multiplyPositionMatrix(transform);
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 1f);
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, banner, ModelLoader.BANNER_BASE, true, patterns, stack.hasGlint() && MiapiConfig.INSTANCE.client.other.enchantingGlint);
                matrices.pop();
            }
            default -> {
                matrices.push();
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.translate(0, -1, 0);
                matrices.push();
                matrices.multiplyPositionMatrix(new Matrix4f(transform));
                matrices.peek().getNormalMatrix().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 16f);
                matrices.scale(1 / 20f, 1 / 20f, 1 / 20f);
                BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, banner, ModelLoader.BANNER_BASE, true, patterns, stack.hasGlint() && MiapiConfig.INSTANCE.client.other.enchantingGlint);
                matrices.pop();
                matrices.pop();
            }
        }
        matrices.pop();
    }

    public static void render(MatrixStack matrices,
                              ItemStack stack,
                              ModelTransformationMode transformationMode,
                              float tickDelta,
                              VertexConsumerProvider vertexConsumers,
                              LivingEntity entity, int light, int overlay,
                              List<BakedQuad> quads,
                              List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns) {
        for (int i = 0; i < 17 && i < patterns.size(); ++i) {
            Pair<RegistryEntry<BannerPattern>, DyeColor> pair = patterns.get(i);
            float[] fs = pair.getSecond().getColorComponents();
            pair.getFirst().getKey().map(TexturedRenderLayers::getBannerPatternTextureId).ifPresent((spriteIdentifier -> {
                var consumer = spriteIdentifier.getVertexConsumer(vertexConsumers, RenderLayer::getEntityNoOutline);
                for (BakedQuad quad : quads) {
                    consumer.quad(matrices.peek(), quad, fs[0], fs[1], fs[2], light, overlay);
                }
            }));
        }
    }

    public static BannerMode getMode(String key) {
        try {
            switch (key.toLowerCase()) {
                case "item" -> {
                    return BannerMode.ITEM;
                }
                case "item_alt" -> {
                    return BannerMode.ITEM_ALT;
                }
            }
            return BannerMode.valueOf(key);
        } catch (Exception e) {
            return BannerMode.MODEL;
        }
    }

    public enum BannerMode {
        MODEL,
        ITEM,
        ITEM_ALT
    }

    public record BannerOnModel(String string, boolean isBanner) {
    }
}
