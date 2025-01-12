package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.ItemInModuleMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.material.MaterialInscribeDataProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemModelProperty extends CodecProperty<List<ModelJson>> {
    public static final ResourceLocation KEY = Miapi.id("item_model");
    public static ItemModelProperty property;
    public static Codec<ModelJson> CODEC = ModelJson.CODEC;//AutoCodec.of(ModelJson.class).codec();
    public static DataComponentType<ItemStack> ITEM_MODEL_COMPONENT = DataComponentType.<ItemStack>builder()
            .persistent(ItemStack.CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(ItemStack.CODEC))
            .build();

    public ItemModelProperty() {
        super(Codec.list(CODEC));
        property = this;
        if(Environment.isClient()){
            clientSetup();
        }
    }
    public void clientSetup(){
        MiapiItemModel.modelSuppliers.add((key,mode, model, stack) -> {
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
                        yield () -> {
                            if (
                                    ModelProperty.isAllowedKey(modelJson.modelType, key) &&
                                    stack.has(DataComponents.CHARGED_PROJECTILES)
                            ) {
                                return stack.get(DataComponents.CHARGED_PROJECTILES).getItems().stream().findFirst().orElse(ItemStack.EMPTY);
                            }
                            return ItemStack.EMPTY;
                        };
                    }
                    default:
                        throw new IllegalStateException("Unexpected value: " + modelJson.type);
                };
                ItemInModuleMiapiModel miapiModel = new ItemInModuleMiapiModel(stackSupplier, modelJson.transform.toMatrix());
                models.add(miapiModel);
            });
            return models;
        });
    }

    @Override
    public List<ModelJson> merge(List<ModelJson> left, List<ModelJson> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

}
