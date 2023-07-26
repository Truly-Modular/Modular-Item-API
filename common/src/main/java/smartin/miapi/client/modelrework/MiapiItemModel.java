package smartin.miapi.client.modelrework;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiapiItemModel implements MiapiModel {
    public List<ModelSupplier> modelSuppliers = new ArrayList<>();
    List<Pair<Matrix4f, MiapiModel>> models;
    Map<String, List<Pair<Matrix4f, MiapiModel>>> otherModels;
    final ItemStack stack;

    public MiapiItemModel(ItemStack stack) {
        this.stack = stack;
        if (stack.getItem() instanceof ModularItem) {
            ItemModule.getModules(stack).allSubModules().forEach(module -> {
                Matrix4f matrix4f = SlotProperty.getLocalTransform(module).toMatrix();
                for (ModelSupplier supplier : modelSuppliers) {
                    supplier.getModels(null, module).forEach(model -> {
                        models.add(new Pair<>(new Matrix4f(matrix4f), model));
                    });
                }
            });
        }
        models = new ArrayList<>();
        otherModels = new HashMap<>();
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModelList(ItemStack stack, String key) {
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        if (stack.getItem() instanceof ModularItem) {
            ItemModule.getModules(stack).allSubModules().forEach(module -> {
                Matrix4f matrix4f = SlotProperty.getLocalTransform(module).toMatrix();
                for (ModelSupplier supplier : modelSuppliers) {
                    supplier.getModels(key, module).forEach(model -> {
                        modelList.add(new Pair<>(new Matrix4f(matrix4f), model));
                    });
                }
            });
        }
        return modelList;
    }

    public void render(String modelType, MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!otherModels.containsKey(modelType)) {
            otherModels.put(modelType, generateModelList(stack, modelType));
        }
        otherModels.get(modelType).forEach(matrix4fMiapiModelPair -> {
            matrices.push();
            matrices.peek().getPositionMatrix().mul(matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, tickDelta, vertexConsumers, light, overlay);
            matrices.pop();
        });
    }

    @Override
    public void render(MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        models.forEach(matrix4fMiapiModelPair -> {
            matrices.push();
            matrices.peek().getPositionMatrix().mul(matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, tickDelta, vertexConsumers, light, overlay);
            matrices.pop();
        });
    }

    interface ModelSupplier {
        List<MiapiModel> getModels(@Nullable String key, ItemModule.ModuleInstance model);
    }
}
