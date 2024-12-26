package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.*;

public class ModuleModel {
    private final Map<ModuleDisplayContext, List<Pair<Matrix4f, MiapiModel>>> cachedModels = new HashMap<>();
    public final ModuleInstance instance;
    public Map<String, ModuleModel> subModuleModels = new HashMap<>();
    public ItemStack stack;

    public ModuleModel(ModuleInstance instance, ItemStack stack) {
        this.instance = instance;
        this.stack = stack;
    }

    private List<Pair<Matrix4f, MiapiModel>> generateModel(String key, ItemDisplayContext context) {
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
        return model;
    }

    private List<Pair<Matrix4f, MiapiModel>> getModel(String key, ItemDisplayContext context) {
        ModuleDisplayContext context1 = new ModuleDisplayContext(key, context);
        return cachedModels.computeIfAbsent(context1, (c) -> generateModel(c.type, c.context));
    }

    public void render(@NonnullDefault String modelType, ItemStack stack, PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        Matrix4f submoduleMatrix = new Matrix4f();
        var moduleModels = getModel(modelType, mode);

        moduleModels.forEach(matrix4fMiapiModelPair -> {
            matrices.pushPose();
            Transform.applyPosition(matrices, matrix4fMiapiModelPair.getFirst());
            matrix4fMiapiModelPair.getSecond().render(matrices, stack, mode, tickDelta, vertexConsumers, entity, light, overlay);
            matrices.popPose();

            submoduleMatrix.mul(matrix4fMiapiModelPair.getSecond().subModuleMatrix());
        });
        //render submodules
        instance.getSubModuleMap().forEach((id, instance1) -> {
            matrices.pushPose();
            Transform.applyPosition(matrices, submoduleMatrix);
            ModuleModel subModuleModel = subModuleModels.get(id);
            if (subModuleModel == null) {
                subModuleModel = new ModuleModel(instance1, stack);
                subModuleModels.put(id, subModuleModel);
            }
            subModuleModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
            matrices.popPose();
        });
    }

    private static class ModuleDisplayContext {
        private final String type;
        private final ItemDisplayContext context;
        private final int hash;

        public ModuleDisplayContext(String type, ItemDisplayContext context) {
            this.type = type == null ? "item" : type;
            this.context = context;
            this.hash = Objects.hash(type, context);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModuleDisplayContext that = (ModuleDisplayContext) o;
            return Objects.equals(type, that.type) && Objects.equals(context, that.context);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}