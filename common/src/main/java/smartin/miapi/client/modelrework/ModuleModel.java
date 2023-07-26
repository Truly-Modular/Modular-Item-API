package smartin.miapi.client.modelrework;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.*;

public class ModuleModel {

    List<Pair<Matrix4f, MiapiModel>> models;
    Map<String, List<Pair<Matrix4f, MiapiModel>>> otherModels;
    final ItemModule.ModuleInstance instance;
    Map<Integer, ModuleModel> subModuleModels = new WeakHashMap<>();

    public ModuleModel(ItemModule.ModuleInstance instance) {
        this.instance = instance;
        models = generateModel(null);
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key) {
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        Matrix4f matrix4f = SlotProperty.getLocalTransformStack(instance).get(key).toMatrix();
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            supplier.getModels(key, instance).forEach(model -> {
                modelList.add(new Pair<>(new Matrix4f(matrix4f), model));
            });
        }
        return modelList;
    }

    public void render(String modelType, MatrixStack matrices, float tickDelta, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!otherModels.containsKey(modelType)) {
            otherModels.put(modelType, generateModel(modelType));
        }
        Map<Integer, Matrix4f> map = new HashMap<>();
        otherModels.get(modelType).forEach(matrix4fMiapiModelPair -> {
            matrices.push();
            matrices.peek().getPositionMatrix().mul(matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, tickDelta, vertexConsumers, light, overlay);
            matrices.pop();
            instance.subModules.forEach((integer, instance1) -> {
                Matrix4f matrix4f = map.getOrDefault(integer, new Matrix4f());
                Matrix4f subModuleMatrix = matrix4fMiapiModelPair.getSecond().subModuleMatrix(integer);
                if (subModuleMatrix != null) {
                    matrix4f.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix(integer));
                }
                map.put(integer, matrix4f);
            });
        });
        instance.subModules.forEach((integer, instance1) -> {
            matrices.push();
            matrices.multiplyPositionMatrix(map.get(integer));
            ModuleModel subModuleModel = subModuleModels.getOrDefault(integer, new ModuleModel(instance1));
            subModuleModel.render(modelType, matrices, tickDelta, vertexConsumers, integer, overlay);
            matrices.pop();
        });
    }
}