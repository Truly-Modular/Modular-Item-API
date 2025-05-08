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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.client.GlintShader;
import smartin.miapi.client.model.item.DualKeyCache;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

@Environment(EnvType.CLIENT)
public class MiapiItemModel implements MiapiModel {
    public static List<ModelSupplier> modelSuppliers = new ArrayList<>();
    public static List<ModelTransformerSupplier> modelTransformersSuppler = new ArrayList<>();
    private static final String CACHE_KEY = "miapi_model_rework";
    public final DualKeyCache<String, ItemDisplayContext, List<ModelTransformer>> transformerCache = new DualKeyCache<>();
    public final DualKeyCache<String, ItemDisplayContext, ModuleModel> modelCache = new DualKeyCache<>();
    public static final WeakHashMap<ItemStack, MiapiItemModel> fallbackLookup = new WeakHashMap<>();

    static {
        ModularItemCache.setSupplier(CACHE_KEY, (s) -> {
            if (fallbackLookup.containsKey(s)) {
                return fallbackLookup.get(s);
            }
            MiapiItemModel model = new MiapiItemModel(s);
            fallbackLookup.put(s, model);
            return model;
        });
    }

    @Nullable
    public static MiapiItemModel getItemModel(ItemStack stack) {
        return ModularItemCache.getRaw(stack, CACHE_KEY);
    }

    private MiapiItemModel(ItemStack stack) {
        if (!(stack.getItem() instanceof VisualModularItem || VisualModularItem.isVisualModularItem(stack))) {
            throw new RuntimeException("Can only make MiapiModel for Modular Items");
        }
    }

    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, int light, int overlay) {
        render(null, stack, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public void render(PoseStack matrices, ItemStack stack, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, LivingEntity entity, int light, int overlay) {
        render(null, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
    }

    public void render(String modelType, ItemStack stack, PoseStack matrices, ItemDisplayContext mode, float tickDelta, MultiBufferSource vertexConsumers, int light, int overlay) {
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
        assert Minecraft.getInstance().level != null;
        Minecraft.getInstance().getProfiler().push("modular_item");
        Minecraft.getInstance().getProfiler().push("root-logic");
        String modelType = modelTypeRaw == null ? "item" : modelTypeRaw;
        ModuleModel rootModel = modelCache.getNullSave(modelType, mode, (s, k) -> new ModuleModel(ItemModule.getModules(stack), stack, s, k));
        matrices.pushPose();
        Minecraft.getInstance().getProfiler().push("model-transformers");
        List<ModelTransformer> transformers = transformerCache.getNullSave(modelType, mode, (s, t) -> getTransfomers(stack, mode, modelType));
        for (ModelTransformer transformer : transformers) {
            matrices = transformer.transform(matrices, tickDelta);
        }
        Minecraft.getInstance().getProfiler().pop();
        if (entity == null) {
            //needed because otherwise overwrites dont work
            entity = Minecraft.getInstance().player;
        }
        Minecraft.getInstance().getProfiler().push("glint-setup");
        GlintShader.setupItem(matrices.last().pose());
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
        //IconRenderProperty.property.renderIcon(ItemModule.getModules(stack), matrices, tickDelta, vertexConsumers, entity, light, overlay);
        rootModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, entity, light, overlay);
        matrices.popPose();
        Minecraft.getInstance().getProfiler().pop();
    }

    private static @NotNull List<ModelTransformer> getTransfomers(ItemStack stack, ItemDisplayContext mode, String modelType) {
        List<ModelTransformer> transformersList = new ArrayList<>();
        modelTransformersSuppler.forEach(modelSupplier -> {
            var transformer = modelSupplier.get(stack, modelType, mode);
            if (transformer != null) {
                transformersList.add(transformer);
            }
        });
        return transformersList;
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix() {
        return null;
    }

    public interface ModelSupplier {
        List<MiapiModel> getModels(String key, @Nullable ItemDisplayContext model, ModuleInstance module, ItemStack stack);

        default List<Pair<Matrix4f, MiapiModel>> filter(List<Pair<Matrix4f, MiapiModel>> models, ItemStack stack, ModuleInstance module, String key, ItemDisplayContext context) {
            return models;
        }
    }

    public interface ModelTransformerSupplier {
        ModelTransformer get(ItemStack itemStack, String modelType, @Nullable ItemDisplayContext itemDisplayContext);
    }

    public interface ModelTransformer {
        PoseStack transform(PoseStack matrices, float tickDelta);
    }
}
