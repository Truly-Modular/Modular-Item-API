package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.MiapiClient;
import smartin.miapi.client.model.ItemMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.item.modular.items.ModularCrossbow;
import smartin.miapi.modules.material.MaterialInscribeDataProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ItemModelProperty extends CodecProperty<List<ModelJson>> {
    public static final ResourceLocation KEY = Miapi.id("item_model");
    public static ItemModelProperty property;
    public static Codec<ModelJson> CODEC = AutoCodec.of(ModelJson.class).codec();
    public static DataComponentType<ItemStack> ITEM_MODEL_COMPONENT = DataComponentType.<ItemStack>builder()
            .persistent(ItemStack.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(ItemStack.CODEC))
            .build();

    public ItemModelProperty() {
        super(Codec.list(CODEC));
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            List<ModelJson> modelJsons = getData(stack).orElse(new ArrayList<>());
            List<MiapiModel> models = new ArrayList<>();
            modelJsons.forEach(modelJson -> {
                Supplier<ItemStack> stackSupplier = switch (modelJson.type) {
                    case "item_nbt": {
                        yield () -> stack.getOrDefault(ITEM_MODEL_COMPONENT, ItemStack.EMPTY);
                    }
                    case "module_data": {
                        if (ModelProperty.isAllowedKey(modelJson.modelType, key)) {
                            yield () -> MaterialInscribeDataProperty.readStackFromModuleInstance(model, modelJson.model);
                        }
                        yield () -> ItemStack.EMPTY;
                    }
                    case "item": {
                        if (ModelProperty.isAllowedKey(modelJson.modelType, key)) {
                            yield () -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(modelJson.model)));
                        }
                        yield () -> ItemStack.EMPTY;
                    }
                    case "projectile": {
                        if (stack.getItem() instanceof ModularCrossbow && ModelProperty.isAllowedKey(modelJson.modelType, key)) {
                            yield () -> ModularCrossbow.getProjectiles(stack).stream().findFirst().orElse(ItemStack.EMPTY);
                        }
                        yield () -> ItemStack.EMPTY;
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + modelJson.type);
                };
                ItemMiapiModel miapiModel = new ItemMiapiModel(stackSupplier, modelJson.transform.toMatrix());
                models.add(miapiModel);
            });
            return models;
        });
    }

    @Override
    public List<ModelJson> merge(List<ModelJson> left, List<ModelJson> right, MergeType mergeType) {
        return ModuleProperty.mergeList(left, right, mergeType);
    }

}
