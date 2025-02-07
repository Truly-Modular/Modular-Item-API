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
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleModel {
    private final List<Pair<Matrix4f, MiapiModel>> actualModels;
    public final ModuleInstance instance;
    public Map<String, ModuleModel> subModuleModels = new HashMap<>();
    public ItemStack stack;
    public final String key;
    @Nullable
    public final ItemDisplayContext context;

    public ModuleModel(ModuleInstance instance, ItemStack stack, String key, @Nullable ItemDisplayContext displayContext) {
        this.instance = instance;
        this.stack = stack;
        actualModels = generateModel(key, displayContext);
        this.key = key;
        this.context = displayContext;
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key, ItemDisplayContext context) {
        Minecraft.getInstance().level.getProfiler().push("generate model");
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        Transform transform = SlotProperty.getTransformStack(instance).get("item".equals(key) ? null : key).copy();
        Matrix4f matrix4f = Transform.toModelTransformation(transform).toMatrix();
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            supplier.getModels(key, context, instance, stack).forEach(model -> {
                modelList.add(new Pair<>(matrix4f, model));
            });
        }
        List<Pair<Matrix4f, MiapiModel>> model = modelList;
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            model = supplier.filter(model, instance, key, context);
        }
        Minecraft.getInstance().level.getProfiler().pop();
        return model;
    }

    public void render(String modelType, ItemStack stack, PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        Minecraft.getInstance().level.getProfiler().push("submodule-logic");
        Matrix4f submoduleMatrix = new Matrix4f();
        Minecraft.getInstance().level.getProfiler().pop();
        actualModels.forEach(matrix4fMiapiModelPair -> {
            Minecraft.getInstance().level.getProfiler().push("submodule-logic");
            matrices.pushPose();
            Transform.applyPosition(matrices, matrix4fMiapiModelPair.getFirst());
            Minecraft.getInstance().level.getProfiler().pop();
            matrix4fMiapiModelPair.getSecond().render(matrices, stack, mode, tickDelta, vertexConsumers, entity, light, overlay);
            Minecraft.getInstance().level.getProfiler().push("submodule-logic");
            matrices.popPose();

            submoduleMatrix.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix());
            Minecraft.getInstance().level.getProfiler().pop();
        });
        //render submodules
        instance.getSubModuleMap().forEach((id, instance1) -> {
            Minecraft.getInstance().level.getProfiler().push("submodule-logic");
            matrices.pushPose();
            Transform.applyPosition(matrices, submoduleMatrix);
            ModuleModel subModuleModel = subModuleModels.get(id);
            if (subModuleModel == null) {
                subModuleModel = new ModuleModel(instance1, stack, key, context);
                subModuleModels.put(id, subModuleModel);
            }
            Minecraft.getInstance().level.getProfiler().pop();
            subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
            matrices.popPose();
        });
    }
}