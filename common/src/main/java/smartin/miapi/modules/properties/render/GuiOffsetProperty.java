package smartin.miapi.modules.properties.render;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This Property allows to have a simple way to change the gui position and size of an item
 */
@Environment(EnvType.CLIENT)
public class GuiOffsetProperty extends CodecProperty<GuiOffsetProperty.GuiOffsetData> {
    public static final String KEY = "guiOffset";
    public static ModuleProperty property;

    public GuiOffsetProperty() {
        super(AutoCodec.of(GuiOffsetData.class).codec());
        property = this;
        ModularItemCache.setSupplier(KEY + "_pure_gui", (stack -> new HashMap<>()));
        MiapiItemModel.modelTransformers.add((matrices, itemStack, mode, modelType, tickDelta) -> {
            if (mode.equals(ItemDisplayContext.GUI)) {
                Map<String, float[]> cache = ModularItemCache.getVisualOnlyCache(itemStack, KEY + "_pure_gui", new HashMap<>());
                float[] data = cache.computeIfAbsent(modelType, s -> getGuiOffsets(itemStack, modelType));
                matrices.translate(data[0], data[1], 0);
                matrices.scale(data[2], data[3], data[4]);

            }
            return matrices;
        });
    }

    public float[] getGuiOffsets(ItemStack itemStack, String modelType) {
        GuiOffsetData guiOffsetJson = new GuiOffsetData();
        for (ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(itemStack))) {
            Optional<GuiOffsetData> optional = getData(instance);
            if (optional.isPresent()) {
                GuiOffsetData add = optional.get();
                Transform transform = SlotProperty.getLocalTransformStack(instance).get(modelType);
                Matrix4f matrix4f = transform.toMatrix();
                Vector4f offsetPos = new Vector4f(add.x, add.y, 0, 0);
                offsetPos = offsetPos.mul(matrix4f);
                guiOffsetJson.x += offsetPos.x();
                guiOffsetJson.y += offsetPos.y();
                Vector4f sizeVec = new Vector4f(add.sizeX, add.sizeY, 0, 0);
                sizeVec = sizeVec.mul(matrix4f);
                guiOffsetJson.sizeX += sizeVec.x();
                guiOffsetJson.sizeY += sizeVec.y();
            }
        }
        float baseSize = 16f;
        guiOffsetJson.x = guiOffsetJson.x / baseSize;
        guiOffsetJson.y = guiOffsetJson.y / baseSize;
        guiOffsetJson.sizeX = (baseSize) / (baseSize + guiOffsetJson.sizeX);
        guiOffsetJson.sizeY = (baseSize) / (baseSize + guiOffsetJson.sizeY);
        float zScale = (guiOffsetJson.sizeX + guiOffsetJson.sizeY) / 2;
        return new float[]{guiOffsetJson.x, guiOffsetJson.y, guiOffsetJson.sizeX, guiOffsetJson.sizeY, zScale};
    }

    @Override
    public GuiOffsetData merge(GuiOffsetData left, GuiOffsetData right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }

    public static class GuiOffsetData {
        public float x = 0;
        public float y = 0;
        public float sizeX = 0;
        public float sizeY = 0;
    }
}
