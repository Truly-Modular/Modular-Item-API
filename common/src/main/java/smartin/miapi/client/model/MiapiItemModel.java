package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.GlintShader;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;

public class MiapiItemModel implements MiapiModel {
    public static List<ModelSupplier> modelSuppliers = new ArrayList<>();
    public static List<ModelTransformer> modelTransformers = new ArrayList<>();
    public final ItemStack stack;
    public final ModuleModel rootModel;
    private static final String CACHE_KEY = "miapi_model_rework";

    static {
        ModularItemCache.setSupplier(CACHE_KEY, (MiapiItemModel::new));
    }

    @Nullable
    public static MiapiItemModel getItemModel(ItemStack stack) {
        return ModularItemCache.getRaw(stack, CACHE_KEY);
    }

    private MiapiItemModel(ItemStack stack) {
        this.stack = stack;
        if (stack.getItem() instanceof VisualModularItem) {
            rootModel = new ModuleModel(ItemModule.getModules(stack), stack);
        } else {
            rootModel = null;
            throw new RuntimeException("Can only make MiapiModel for Modular Items");
        }
    }

    public void render(PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, int light, int overlay) {
        render(null, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        render(null, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
    }

    public void render(String modelType, PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, int light, int overlay) {
        render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, null, light, overlay);
    }

    public void render(
            String modelTypeRaw,
            ItemStack stack,
            PoseStack matrices,
            ItemDisplayContext mode,
            float tickDelta,
            MultiBufferSource vertexConsumers,
            LivingEntity entity,
            int light,
            int overlay) {
        if (ReloadEvents.isInReload()) return;

        String modelType = modelTypeRaw == null ? "item" : modelTypeRaw;
        Minecraft.getInstance().level.getProfiler().push("modular_item");
        matrices.pushPose();
        for (ModelTransformer transformer : modelTransformers) {
            matrices = transformer.transform(matrices, stack, modelType, mode, tickDelta);
        }
        if (entity == null) {
            //needed because otherwise overwrites dont work
            entity = Minecraft.getInstance().player;
        }
        GlintShader.setupItem(matrices.last().pose());
        rootModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
        matrices.popPose();
        Minecraft.getInstance().level.getProfiler().pop();
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix() {
        return null;
    }

    public interface ModelSupplier {
        List<MiapiModel> getModels(String key, @Nullable ItemDisplayContext model, ModuleInstance module, ItemStack stack);

        default List<Pair<Matrix4f, MiapiModel>> filter(List<Pair<Matrix4f, MiapiModel>> models, ModuleInstance module, String key, ItemDisplayContext context) {
            return models;
        }
    }

    public interface ModelTransformer {
        PoseStack transform(PoseStack matrices, ItemStack itemStack, String modelType, @Nullable ItemDisplayContext model, float tickDelta);
    }
}
