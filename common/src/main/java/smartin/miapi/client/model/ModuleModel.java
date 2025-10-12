package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class ModuleModel {
    private final List<Pair<Matrix4f, MiapiModel>> actualModels;
    public final ModuleInstance instance;
    public Map<String, ModuleModel> subModuleModels = new HashMap<>();
    public ItemStack stack;
    public final String key;
    @Nullable
    public final ItemDisplayContext context;
    public boolean renderSubmodules = true;
    public boolean simpleSubModules = true;

    public ModuleModel(ModuleInstance instance, ItemStack stack, String key, @Nullable ItemDisplayContext displayContext) {
        this.instance = instance;
        this.stack = stack;
        actualModels = generateModel(key, displayContext);
        this.key = key;
        this.context = displayContext;
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key, ItemDisplayContext context) {
        Minecraft.getInstance().getProfiler().push("generate model");
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        Transform transform = SlotProperty.getTransformStack(instance).get("item".equals(key) ? null : key).copy();
        Matrix4f matrix4f = Transform.toModelTransformation(transform).toMatrix();
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            supplier.getModels(key, context, instance, stack).forEach(model -> {
                modelList.add(new Pair<>(matrix4f, model));
                if (model.hasAnimatedModuleMatrix()) {

                }
            });
        }
        for (Pair<Matrix4f, MiapiModel> pair : modelList) {
            if (pair.getSecond().hasAnimatedModuleMatrix()) {
                simpleSubModules = false;
            }
        }
        List<Pair<Matrix4f, MiapiModel>> model = modelList;
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            model = supplier.filter(model, stack, instance, key, context);
        }
        Minecraft.getInstance().getProfiler().pop();
        return model;
    }

    public void render(String modelType, ItemStack stack, PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push("submodule-logic");
        Matrix4f submoduleMatrix = new Matrix4f();
        Minecraft.getInstance().getProfiler().pop();
        actualModels.forEach(matrix4fMiapiModelPair -> {
            Minecraft.getInstance().getProfiler().push("submodule-logic");
            matrices.pushPose();
            Transform.applyPosition(matrices, matrix4fMiapiModelPair.getFirst());
            Minecraft.getInstance().getProfiler().pop();
            matrix4fMiapiModelPair.getSecond().render(matrices, stack, mode, tickDelta, vertexConsumers, entity, light, overlay);
            Minecraft.getInstance().getProfiler().push("submodule-logic");
            matrices.popPose();
            if(!simpleSubModules){
                submoduleMatrix.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix());
            }
            Minecraft.getInstance().getProfiler().pop();
        });
        //render submodules
        if (renderSubmodules) {
            if (simpleSubModules) {
                instance.getSubModuleMap().forEach((id, instance1) -> {
                    Minecraft.getInstance().getProfiler().push("submodule-logic");
                    matrices.pushPose();
                    ModuleModel subModuleModel = subModuleModels.get(id);
                    if (subModuleModel == null) {
                        subModuleModel = new ModuleModel(instance1, stack, key, context);
                        subModuleModels.put(id, subModuleModel);
                    }
                    Minecraft.getInstance().getProfiler().pop();
                    subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
                    matrices.popPose();
                });
            }
            instance.getSubModuleMap().forEach((id, instance1) -> {
                Minecraft.getInstance().getProfiler().push("submodule-logic");
                matrices.pushPose();
                Transform.applyPosition(matrices, submoduleMatrix);
                ModuleModel subModuleModel = subModuleModels.get(id);
                if (subModuleModel == null) {
                    subModuleModel = new ModuleModel(instance1, stack, key, context);
                    subModuleModels.put(id, subModuleModel);
                }
                Minecraft.getInstance().getProfiler().pop();
                subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
                matrices.popPose();
            });
        }
    }
}