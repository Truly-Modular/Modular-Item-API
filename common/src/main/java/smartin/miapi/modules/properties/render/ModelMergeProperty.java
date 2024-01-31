package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformMap;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.SlotProperty;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This Property allows to merge different Modelparts together
 */
@Environment(EnvType.CLIENT)
public class ModelMergeProperty implements ModuleProperty {
    public static final String KEY = "modelMerge";
    public static ModuleProperty property;

    public ModelMergeProperty() {
        property = this;
        ModelProperty.modelTransformers.add(
                new ModelProperty.ModelTransformer() {
                    @Override
                    public List<ModelProperty.TransformedUnbakedModel> unBakedTransform(List<ModelProperty.TransformedUnbakedModel> list, ItemStack stack) {
                        ModuleInstance root = ItemModule.getModules(stack);
                        List<Json> toMerge = new ArrayList<>();
                        root.allSubModules().forEach(moduleInstance -> {
                            JsonElement data = moduleInstance.getProperties().get(property);
                            if (data != null) {
                                Type type = new TypeToken<List<Json>>() {
                                }.getType();
                                List<Json> jsonDatas = Miapi.gson.fromJson(data, type);
                                jsonDatas.forEach(jsonData -> {
                                    TransformMap transformMap = SlotProperty.getTransformStack(SlotProperty.getSlotIn(moduleInstance));
                                    if (jsonData.transform == null) {
                                        jsonData.transform = Transform.IDENTITY;
                                    } else {
                                        jsonData.transform.origin = null;
                                    }
                                    Transform from = transformMap.get(jsonData.from).copy();
                                    from = from.merge(jsonData.transform);
                                    jsonData.transform = from;
                                    toMerge.add(jsonData);
                                });
                            }
                        });

                        List<ModelProperty.TransformedUnbakedModel> newList = new ArrayList<>(list);
                        list.forEach(unbakedModel -> {
                            toMerge.forEach(json -> {
                                if (json.from.equals(unbakedModel.transform().primary)) {
                                    TransformMap stack1 = unbakedModel.transform().copy();
                                    stack1 = new TransformMap();
                                    stack1.add(json.to, unbakedModel.transform().get());
                                    stack1.add(json.to, stack1.get());
                                    stack1.primary = json.to;
                                    ModelProperty.TransformedUnbakedModel transformedUnbakedModel1 = new ModelProperty.TransformedUnbakedModel(stack1, unbakedModel.unbakedModel(), unbakedModel.instance(), unbakedModel.color());
                                    newList.add(transformedUnbakedModel1);
                                }
                            });
                        });
                        return newList;
                    }
                });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    static class Json {
        public String from;
        public String to;
        public Transform transform;
    }
}
