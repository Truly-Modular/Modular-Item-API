package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import smartin.miapi.Miapi;
import smartin.miapi.client.modelrework.BannerMiapiModel;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.client.modelrework.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialInscribeDataProperty;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BannerModelProperty implements ModuleProperty {
    public static final String KEY = "banner";
    public static BannerModelProperty property;

    public BannerModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, moduleInstance, stack) -> {
            JsonElement element = moduleInstance.getProperties().get(property);
            List<MiapiModel> models = new ArrayList<>();
            if (element != null) {
                element.getAsJsonArray().forEach(element1 -> {
                    ModelJson modelJson = Miapi.gson.fromJson(element1, ModelJson.class);
                    if ("parent".equals(modelJson.modelType)) {
                        SlotProperty.ModuleSlot slot = SlotProperty.getSlotIn(moduleInstance);
                        if (slot != null) {
                            modelJson.modelType = slot.transform.origin;
                        }
                    }
                    Supplier<ItemStack> stackSupplier = switch (modelJson.type) {
                        case "item_nbt": {
                            NbtCompound itemCompound = stack.getOrCreateNbt().getCompound("banner");
                            if (!itemCompound.isEmpty() && ModelProperty.isAllowedKey(modelJson.modelType, key)) {
                                yield () -> ItemStack.fromNbt(itemCompound);
                            }
                            yield () -> ItemStack.EMPTY;
                        }
                        case "module_data": {
                            if (ModelProperty.isAllowedKey(modelJson.modelType, key)) {
                                yield () -> MaterialInscribeDataProperty.readStackFromModuleInstance(moduleInstance, "banner");
                            }
                            yield () -> ItemStack.EMPTY;
                        }
                        default:
                            throw new IllegalStateException("Unexpected value: " + modelJson.type);
                    };
                    BannerMiapiModel.BannerMode mode = BannerMiapiModel.getMode(modelJson.model);
                    modelJson.transform = Transform.repair(modelJson.transform);
                    BannerMiapiModel bannerMiapiModel = BannerMiapiModel.getFromStack(stackSupplier.get(), mode, modelJson.transform.toMatrix());
                    if (bannerMiapiModel != null) {
                        models.add(bannerMiapiModel);
                    }
                });
            }
            return models;
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    static class ModelJson {
        public String type;
        public String model;
        public String modelType;
        public Transform transform = Transform.IDENTITY;
    }
}
