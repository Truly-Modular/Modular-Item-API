package smartin.miapi.modules.properties.render;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.module.ItemInModuleMiapiModel;
import smartin.miapi.client.model.MiapiItemModel;
import smartin.miapi.client.model.MiapiModel;
import smartin.miapi.material.MaterialInscribeDataProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.baked.ModelProperty;
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
        if (Environment.isClient()) {
            clientSetup();
        }
    }

    public void clientSetup() {
        MiapiItemModel.modelSuppliers.add((key, mode, model, stack) -> {
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
                        yield getProjectileSupplier(key, mode, model, stack, modelJson);
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

    public static Supplier<ItemStack> getProjectileSupplier(String key, @Nullable ItemDisplayContext model, ModuleInstance module, ItemStack stack, ModelJson modelJson) {
        return new Supplier<>() {
            int hash = 0;
            ItemStack cache = null;

            @Override
            public ItemStack get() {
                Minecraft.getInstance().getProfiler().push("projectile");
                Minecraft.getInstance().getProfiler().push("getProjectile");
                var component = stack.get(DataComponents.CHARGED_PROJECTILES);
                Minecraft.getInstance().getProfiler().pop();
                if (
                        ModelProperty.isAllowedKey(modelJson.modelType, key) &&
                        component != null
                ) {
                    if (hash == component.hashCode() && cache != null) {
                        //optimize to avoid component logic
                        return cache;
                    }
                    assert component != null;
                    Minecraft.getInstance().getProfiler().push("getItem");
                    var items = component.getItems();
                    var optional = items.stream().findFirst();
                    Minecraft.getInstance().getProfiler().pop();
                    if (optional.isPresent()) {
                        if (cache != null && ItemStack.isSameItem(optional.get(), cache)) {
                            Minecraft.getInstance().getProfiler().pop();
                            return cache;
                        } else {
                            cache = optional.get();
                            hash = component.hashCode();
                        }
                    }
                    Minecraft.getInstance().getProfiler().pop();
                    return stack.get(DataComponents.CHARGED_PROJECTILES).getItems().stream().findFirst().orElse(ItemStack.EMPTY);
                }
                Minecraft.getInstance().getProfiler().pop();
                return ItemStack.EMPTY;
            }
        };
    }

    @Override
    public List<ModelJson> merge(List<ModelJson> left, List<ModelJson> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }

}
