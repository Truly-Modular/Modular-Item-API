package smartin.miapi.client.modelrework;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleModel {
    public List<Pair<Matrix4f, MiapiModel>> models;
    public Map<String, List<Pair<Matrix4f, MiapiModel>>> otherModels;
    public final ModuleInstance instance;
    public Map<Integer, ModuleModel> subModuleModels = new HashMap<>();
    public ItemStack stack;

    public ModuleModel(ModuleInstance instance, ItemStack stack) {
        this.instance = instance;
        this.stack = stack;
        models = generateModel(null);
        otherModels = new HashMap<>();
        otherModels.put("item", models);
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key) {
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        Transform transform = SlotProperty.getTransformStack(instance).get(key).copy();
        Matrix4f matrix4f = Transform.toModelTransformation(transform).toMatrix();
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            supplier.getModels(key, instance, stack).forEach(model -> {
                modelList.add(new Pair<>(matrix4f, model));
            });
        }
        return modelList;
    }

    public void render(String modelTypeRaw, ItemStack stack, MatrixStack matrices, ModelTransformationMode mode, float tickDelta, VertexConsumerProvider vertexConsumers, LivingEntity entity, int light, int overlay) {
        String modelType = modelTypeRaw == null ? "item" : modelTypeRaw;
        if (!otherModels.containsKey(modelType)) {
            otherModels.put(modelType, generateModel(modelType));
        }
        Matrix4f submoduleMatrix = new Matrix4f();

        otherModels.get(modelType).forEach(matrix4fMiapiModelPair -> {
            matrices.push();
            Transform.applyPosition(matrices,matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, stack, mode, tickDelta, vertexConsumers, entity, light, overlay);
            matrices.pop();

            submoduleMatrix.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix());
        });
        //render submodules
        instance.subModules.forEach((integer, instance1) -> {
            matrices.push();
            Transform.applyPosition(matrices,submoduleMatrix);
            ModuleModel subModuleModel = subModuleModels.get(integer);
            if (subModuleModel == null) {
                subModuleModel = new ModuleModel(instance1, stack);
                subModuleModels.put(integer, subModuleModel);
            }
            subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
            matrices.pop();
        });
    }
}