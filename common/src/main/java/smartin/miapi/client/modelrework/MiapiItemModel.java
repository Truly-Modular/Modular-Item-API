package smartin.miapi.client.modelrework;

import com.redpxnda.nucleus.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;

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
            rootModel = new ModuleModel(ItemModule.getModules(stack));
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
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(new Identifier("nucleus", "item/blank"));
        for (int i = 0; i < 6; i++) {
            RenderUtil.addQuad(
                    RenderUtil.CUBE[i], matrices, vertexConsumers.getBuffer(RenderLayer.getTranslucent()),
                    1f, 1f, 1f, 1f,
                    -.1f, .1f, .1f,
                    sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV(),
                    light);
        }
        //rootModel.render(modelType, stack, matrices, mode, tickDelta, vertexConsumers, light, overlay);
    }

    @Override
    public @Nullable Matrix4f subModuleMatrix(int submoduleId) {
        return null;
    }

    interface ModelSupplier {
        List<MiapiModel> getModels(@Nullable String key, ItemModule.ModuleInstance model);
    }
}
