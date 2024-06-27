package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Holder;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class BannerMiapiModel implements MiapiModel {
    ModelPart banner;
    List<Pair<Holder<BannerPattern>, DyeColor>> patterns;
    BannerMode mode;
    Matrix4f transform;

    public BannerMiapiModel(List<Pair<Holder<BannerPattern>, DyeColor>> patterns, BannerMode mode, Matrix4f transform) {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, 0.0F, -2.0F, 20.0F, 40.0F, 1.0F), PartPose.ZERO);
        this.banner = modelPartData.getChild("flag").bake(64, 64);
        this.patterns = patterns;
        this.mode = mode;
        this.transform = transform;
    }

    @Nullable
    public static BannerMiapiModel getFromStack(ItemStack stack, BannerMode mode, Matrix4f transform) {
        if (stack.getItem() instanceof BannerItem bannerItem) {
            DyeColor dyeColor = bannerItem.getColor();
            ListTag patternList = BannerBlockEntity.getPatternListNbt(stack);
            if (dyeColor != null && patternList != null) {
                if (mode == null) {
                    mode = BannerMode.MODEL;
                }
                return new BannerMiapiModel(BannerBlockEntity.getPatternsFromNbt(dyeColor, patternList), mode, transform);
            }
        }
        return null;
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext transformationMode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        matrices.pushPose();
        switch (mode) {
            case ITEM -> {
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.pushPose();
                matrices.translate(8, -8, -8.75);
                matrices.scale(1, 1, -1);
                matrices.mulPose(transform);
                matrices.last().normal().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, banner, ModelBakery.BANNER_BASE, true, patterns, stack.hasFoil());
                matrices.popPose();

                matrices.pushPose();
                matrices.translate(8, -8, -7.25);
                matrices.scale(1, 1, 1);
                matrices.mulPose(transform);
                matrices.last().normal().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, banner, ModelBakery.BANNER_BASE, true, patterns, stack.hasFoil());
                matrices.popPose();
            }
            case ITEM_ALT -> {
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.pushPose();
                matrices.translate(8, -8, -8.75);
                matrices.scale(1, 1, -1);
                matrices.mulPose(transform);
                matrices.mulPose(Axis.XP.rotationDegrees(10));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 2f);
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, banner, ModelBakery.BANNER_BASE, true, patterns, stack.hasFoil());
                matrices.popPose();

                matrices.pushPose();
                matrices.translate(8, -8, -7.25);
                matrices.scale(1, 1, 1);
                matrices.mulPose(transform);
                matrices.mulPose(Axis.XP.rotationDegrees(10));
                matrices.scale(16f, 16f, 1f);
                matrices.scale(1 / 20f, 1 / 20f, 1f);
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, banner, ModelBakery.BANNER_BASE, true, patterns, stack.hasFoil());
                matrices.popPose();
            }
            default -> {
                matrices.pushPose();
                matrices.scale(1 / 16f, -1 / 16f, -1 / 16f);
                matrices.translate(0, -1, 0);
                matrices.pushPose();
                matrices.mulPose(new Matrix4f(transform));
                matrices.last().normal().mul(transform.get3x3(new Matrix3f()));
                matrices.scale(16f, 16f, 16f);
                matrices.scale(1 / 20f, 1 / 20f, 1 / 20f);
                BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, banner, ModelBakery.BANNER_BASE, true, patterns, stack.hasFoil());
                matrices.popPose();
                matrices.popPose();
            }
        }
        matrices.popPose();
    }

    public static BannerMode getMode(String key) {
        try {
            switch (key.toLowerCase()){
                case "item"-> {
                    return BannerMode.ITEM;
                }
                case "item_alt" ->{
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
}
