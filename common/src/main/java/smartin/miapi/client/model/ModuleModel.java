package smartin.miapi.client.model;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.model.collision.Ray;
import smartin.miapi.client.model.collision.RayHit;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.slot.SlotProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ModuleModel {
    public boolean renderSubmodules = true;
    public boolean simpleSubModules = true;
    public final Matrix4f staticSubModuleMatrix;
    public final ModelWrapper[] currentModuleModels;
    public final ModuleModel[] subModuleModules;
    public final ModuleInstance instance;

    public ModuleModel(ModuleInstance instance, ItemStack stack, String key, @Nullable ItemDisplayContext displayContext) {
        this.instance = instance;
        Transform transform = SlotProperty.getTransformStack(instance).get("item".equals(key) ? null : key).copy();
        staticSubModuleMatrix = Transform.toModelTransformation(transform).toMatrix();
        List<Pair<Matrix4f, MiapiModel>> modelList = new ArrayList<>();
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            for (MiapiModel model : supplier.getModels(key, displayContext, instance, stack)) {
                modelList.add(Pair.of(new Matrix4f(staticSubModuleMatrix), model));
                if (model.hasAnimatedModuleMatrix()) {
                    simpleSubModules = false;
                }
            }
        }
        for (MiapiItemModel.ModelSupplier supplier : MiapiItemModel.modelSuppliers) {
            modelList = supplier.filter(modelList, stack, instance, key, displayContext);
        }
        currentModuleModels = new ModelWrapper[modelList.size()];
        for (int i = 0; i < modelList.size(); i++) {
            Pair<Matrix4f, MiapiModel> pair = modelList.get(i);
            currentModuleModels[i] =
                    new ModelWrapper(pair.getFirst(), pair.getSecond());
        }
        List<ModuleModel> subModules = new ArrayList<>();
        instance.cache().getSubModules().forEach((id, module) -> {
            subModules.add(new ModuleModel(module, stack, key, displayContext));
        });
        subModuleModules = subModules.toArray(new ModuleModel[subModules.size()]);
    }

    public void render(MiapiModel.RenderContext context) {
        for (ModelWrapper currentModuleModel : currentModuleModels) {
            context.matrices().pushPose();
            Transform.applyPosition(context.matrices(), currentModuleModel.matrix4f);
            currentModuleModel.miapiModel.render(context);
            context.matrices().popPose();
        }
        context.matrices().pushPose();
        if (!simpleSubModules) {
            Matrix4f submoduleMatrix = new Matrix4f();
            for (ModelWrapper currentModuleModel : currentModuleModels) {
                if (currentModuleModel.miapiModel.hasAnimatedModuleMatrix()) {
                    submoduleMatrix.mul(currentModuleModel.miapiModel.subModuleMatrix(context));
                }
            }
            context.matrices().mulPose(submoduleMatrix);
        }
        if (renderSubmodules) {
            for (ModuleModel model : subModuleModules) {
                context.matrices().pushPose();
                model.render(context);
                context.matrices().popPose();
            }
        }
        context.matrices().popPose();
    }

    public Optional<ModuleRayHit> raycast(Ray ray, MiapiModel.RenderContext context) {
        ModuleRayHit closest = null;
        for (ModelWrapper currentModuleModel : currentModuleModels) {
            context.matrices().pushPose();
            Transform.applyPosition(context.matrices(), currentModuleModel.matrix4f);
            Optional<RayHit> rayHit = currentModuleModel.miapiModel().raycast(ray, context);
            if (rayHit.isPresent()) {
                if (closest == null || closest.hit.distance() > rayHit.get().distance()) {
                    closest = new ModuleRayHit(instance, currentModuleModel.miapiModel(), rayHit.get());
                }
            }
            context.matrices().popPose();
        }
        context.matrices().pushPose();
        if (!simpleSubModules) {
            Matrix4f submoduleMatrix = new Matrix4f();
            for (ModelWrapper currentModuleModel : currentModuleModels) {
                if (currentModuleModel.miapiModel.hasAnimatedModuleMatrix()) {
                    submoduleMatrix.mul(currentModuleModel.miapiModel.subModuleMatrix(context));
                }
            }
            context.matrices().mulPose(submoduleMatrix);
        }
        if (renderSubmodules) {
            for (ModuleModel model : subModuleModules) {
                context.matrices().pushPose();
                Optional<ModuleRayHit> rayHit = model.raycast(ray, context);
                if (rayHit.isPresent()) {
                    if (closest == null || closest.hit.distance() > rayHit.get().hit().distance()) {
                        closest = rayHit.get();
                    }
                }
                context.matrices().popPose();
            }
        }
        context.matrices().popPose();

        return Optional.ofNullable(closest);
    }

    public record ModuleRayHit(ModuleInstance module, MiapiModel model, RayHit hit) {}

    public record ModelWrapper(Matrix4f matrix4f, MiapiModel miapiModel) {}
}