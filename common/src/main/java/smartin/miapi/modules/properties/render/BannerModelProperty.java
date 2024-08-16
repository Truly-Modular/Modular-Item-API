package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.BannerMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.material.MaterialInscribeDataProperty;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class BannerModelProperty extends CodecProperty<List<BannerModelProperty.BannerModelData>> {
    public static final ResourceLocation KEY = Miapi.id("banner");
    public static BannerModelProperty property;
    public static Codec<List<BannerModelProperty.BannerModelData>> CODEC = Codec.list(AutoCodec.of(BannerModelData.class).codec());

    public BannerModelProperty() {
        super(CODEC);
        property = this;
        MiapiItemModel.modelSuppliers.add((key, moduleInstance, stack) -> {
            List<MiapiModel> models = new ArrayList<>();
            getData(moduleInstance).ifPresent(list -> {
                list.forEach(bannerModelData -> {
                    if ("parent".equals(bannerModelData.modelType)) {
                        SlotProperty.ModuleSlot slot = SlotProperty.getSlotIn(moduleInstance);
                        if (slot != null) {
                            bannerModelData.modelType = slot.transform.origin;
                        }
                    }
                    Supplier<ItemStack> stackSupplier = switch (bannerModelData.type) {
                        case "item_nbt": {
                            yield () -> stack;
                        }
                        case "module_data": {
                            if (ModelProperty.isAllowedKey(bannerModelData.modelType, key)) {
                                yield () -> MaterialInscribeDataProperty.readStackFromModuleInstance(moduleInstance, KEY.toString());
                            }
                            yield () -> ItemStack.EMPTY;
                        }
                        default:
                            throw new IllegalStateException("Unexpected value: " + bannerModelData.type);
                    };
                    BannerMiapiModel.BannerMode mode = BannerMiapiModel.getMode(bannerModelData.model);
                    bannerModelData.transform = Transform.repair(bannerModelData.transform);
                    BannerMiapiModel bannerMiapiModel = BannerMiapiModel.getFromStack(stackSupplier.get(), mode, bannerModelData.transform.toMatrix());
                    if (bannerMiapiModel != null) {
                        models.add(bannerMiapiModel);
                    }
                });
            });
            return models;
        });
    }

    @Override
    public List<BannerModelData> merge(List<BannerModelData> left, List<BannerModelData> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

    public static class BannerModelData {
        public String type;
        public String model;
        public String modelType;
        public Transform transform = Transform.IDENTITY;
    }
}
