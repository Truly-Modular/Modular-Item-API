package smartin.miapi.client.modelrework;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;

import java.util.ArrayList;
import java.util.List;

public class MiapiItemModel implements MiapiModel {
    public static List<ModelSupplier> modelSuppliers = new ArrayList<>();
    public final ItemStack stack;
    public final ModuleModel rootModel;

    public MiapiItemModel(ItemStack stack) {
        this.stack = stack;
        if (stack.getItem() instanceof ModularItem) {
            rootModel = new ModuleModel(ItemModule.getModules(stack));
        } else {
            rootModel = null;
            throw new RuntimeException("Can only make MiapiModel for Modular Items");
        }
    }

    public void render(MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(null, matrices, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public void render(MatrixStack matrices, ItemStack stack, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(null, stack, matrices, tickDelta, vertexConsumers, light, overlay);
    }

    public void render(String modelType, MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(modelType, stack, matrices, tickDelta, vertexConsumers, light, overlay);
    }

    public void render(String modelType, ItemStack stack, MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        rootModel.render(modelType, stack, matrices, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return null;
    }

    interface ModelSupplier {
        List<MiapiModel> getModels(@Nullable String key, ItemModule.ModuleInstance model);
    }
}
