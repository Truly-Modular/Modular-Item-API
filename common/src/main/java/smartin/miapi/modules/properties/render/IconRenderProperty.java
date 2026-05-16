package smartin.miapi.modules.properties.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.client.model.ModuleModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;


public class IconRenderProperty extends CodecProperty<String> {
    public static final ResourceLocation KEY = Miapi.id("module_icon");
    public static IconRenderProperty property;
    public static String CACHE_KEY = KEY.toString() + "_ModuleModel";

    public IconRenderProperty() {
        super(Codec.STRING);
        property = this;
        if (smartin.miapi.Environment.isClient()) {
            setupClient();
        }
    }

    @Environment(EnvType.CLIENT)
    public void setupClient() {
        ModularItemCache.MODULE_CACHE_SUPPLIER.put(CACHE_KEY, (m) -> {
            Matrix4f matrix4f = new Matrix4f();
            boolean hasIcon = IconRenderProperty.property.getData(m.owner()).isPresent();
            if (!hasIcon) {
                Optional<GuiOffsetProperty.GuiOffsetData> optional = GuiOffsetProperty.property.getData(m.owner());
                if (optional.isPresent()) {
                    Transform merge = new Transform(
                            new Vector3f(0, 0, 0),
                            new Vector3f(optional.get().x / 16, optional.get().y / 16, 0),
                            new Vector3f(1 + optional.get().sizeX / 16, 1 + optional.get().sizeX / 16, 1));
                    matrix4f = merge.toMatrix();
                }
            }
            String type = IconRenderProperty.property.getData(m.owner()).orElse("item");
            ModuleModel model = new ModuleModel(m.owner(), ItemStack.EMPTY, type, ItemDisplayContext.GUI);
            model.renderSubmodules = false;
            return new RenderContext(matrix4f, type, model);
        });
    }

    @Environment(EnvType.CLIENT)
    public void renderIcon(ModuleInstance stack,
                           PoseStack matrices,
                           float tickDelta,
                           MultiBufferSource vertexConsumers,
                           LivingEntity entity,
                           int light,
                           int overlay) {
        RenderContext model = stack.cache().getFromCache(CACHE_KEY, () -> new RenderContext(new Matrix4f(), "item", new ModuleModel(stack, ItemStack.EMPTY, "item", ItemDisplayContext.GUI)));
        matrices.mulPose(model.matrix4f());
        model.model().render(new MiapiModel.RenderContext(model.type(), matrices, ItemStack.EMPTY, ItemDisplayContext.GUI, tickDelta, vertexConsumers, entity,false, light, overlay));
    }

    public RenderContext getContext(ModuleInstance moduleInstance) {
        ModuleModel model = new ModuleModel(moduleInstance, ItemStack.EMPTY, "item", ItemDisplayContext.GUI);
        model.renderSubmodules = false;
        return new RenderContext(new Matrix4f(), "item", model);
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    public record RenderContext(Matrix4f matrix4f, String type, ModuleModel model) {

    }
}
