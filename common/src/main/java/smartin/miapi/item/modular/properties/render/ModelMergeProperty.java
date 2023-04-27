package smartin.miapi.item.modular.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.TransformStack;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.lang.reflect.Type;
import java.util.List;

public class ModelMergeProperty implements ModuleProperty {
    public static final String KEY = "modelMerge";
    public static ModuleProperty property;

    public ModelMergeProperty() {
        property = this;
        ModelProperty.modelTransformers.add((dynamicBakedModelmap, stack) -> {
            ItemModule.ModuleInstance root = ItemModule.getModules(stack);
            root.allSubModules().forEach(moduleInstance -> {
                JsonElement data = moduleInstance.getProperties().get(property);
                if (data != null) {
                    Type type = new TypeToken<List<Json>>() {
                    }.getType();
                    List<Json> jsonDatas = Miapi.gson.fromJson(data, type);
                    jsonDatas.forEach(jsonData->{
                        TransformStack transformStack = SlotProperty.getTransformStack(SlotProperty.getSlotIn(moduleInstance));
                        if(jsonData.transform==null){
                            jsonData.transform = Transform.IDENTITY;
                        }
                        //Transform transform = transformStack.get(jsonData.to).merge(jsonData.transform);
                        //this transform should be applied
                        //TODO:apply transform on DynamicBakedModel
                        DynamicBakedModel addModel = dynamicBakedModelmap.get(jsonData.from);
                        if(addModel!=null){
                            DynamicBakedModel model = dynamicBakedModelmap.get(jsonData.to);
                            if (model == null) {
                                model = dynamicBakedModelmap.get(jsonData.from);
                            } else {
                                model.addModel(dynamicBakedModelmap.get(jsonData.from));
                            }
                            if(model!=null){
                                dynamicBakedModelmap.put(jsonData.to, model);
                            }
                        }
                    });
                }
            });
            return dynamicBakedModelmap;
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
