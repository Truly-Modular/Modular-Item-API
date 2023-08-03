package smartin.miapi.client.modelrework;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.render.ModelTransformationProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;

public class MiapiItemModel implements MiapiModel {
    public static List<ModelSupplier> modelSuppliers = new ArrayList<>();
    public final ItemStack stack;
    public final ModuleModel rootModel;
    private final static String cacheKey = "miapi_model_rework";

    static {
        ModularItemCache.setSupplier(cacheKey, (MiapiItemModel::new));
    }

    public static MiapiItemModel getItemModel(ItemStack stack) {
        return (MiapiItemModel) ModularItemCache.get(stack, cacheKey);
    }

    private MiapiItemModel(ItemStack stack) {
        this.stack = stack;
        if (stack.getItem() instanceof ModularItem) {
            rootModel = new ModuleModel(ItemModule.getModules(stack), stack);
        } else {
            rootModel = null;
            throw new RuntimeException("Can only make MiapiModel for Modular Items");
        }
    }

    public void render(MatrixStack matrices, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(null, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(null, stack, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    public void render(String modelType, MatrixStack matrices, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    public void render(String modelType, ItemStack stack, MatrixStack matrices, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        RegistryInventory.Client.glintShader.getUniformOrDefault("ModelMat").set(new Matrix4f(matrices.peek().getPositionMatrix()));
        matrices.push();
        ModelTransformationProperty.applyTransformation(stack, mode, matrices);
        rootModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, light, overlay);
        matrices.pop();
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return null;
    }

    public interface ModelSupplier {
        List<MiapiModel> getModels(@Nullable String key, ItemModule.ModuleInstance model, ItemStack stack);
    }
}
