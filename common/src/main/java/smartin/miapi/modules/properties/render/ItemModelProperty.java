package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.client.modelrework.ItemMiapiModel;
import smartin.miapi.client.modelrework.MiapiItemModel;
import smartin.miapi.client.modelrework.MiapiModel;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.items.ModularCrossbow;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ItemModelProperty implements ModuleProperty {
    public static final String KEY = "item_model";
    public static ItemModelProperty property;

    public ItemModelProperty() {
        property = this;
        MiapiItemModel.modelSuppliers.add((key, model, stack) -> {
            JsonElement element = model.getProperties().get(property);
            List<MiapiModel> models = new ArrayList<>();
            if (element != null) {
                element.getAsJsonArray().forEach(element1 -> {
                    ModelJson modelJson = Miapi.gson.fromJson(element1, ModelJson.class);
                    Supplier<ItemStack> stackSupplier = switch (modelJson.type) {
                        case "item_nbt":{
                            NbtCompound itemCompound = stack.getOrCreateNbt().getCompound(modelJson.model);
                            if (!itemCompound.isEmpty()) {
                                yield () -> ItemStack.fromNbt(itemCompound);
                            }
                            yield () -> ItemStack.EMPTY;
                        }
                        case "item" : {
                            yield () -> new ItemStack(Registries.ITEM.get(new Identifier(modelJson.model)));
                        }
                        case "projectile" : {
                            if(stack.getItem() instanceof ModularCrossbow){
                                yield () -> ModularCrossbow.getProjectiles(stack).stream().findFirst().orElse(ItemStack.EMPTY);
                            }
                        }
                        default:
                            throw new IllegalStateException("Unexpected value: " + modelJson.type);
                    };
                    ItemMiapiModel miapiModel = new ItemMiapiModel(stackSupplier, modelJson.transform.toMatrix());
                    models.add(miapiModel);
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
        public Transform transform = Transform.IDENTITY;
    }
}
