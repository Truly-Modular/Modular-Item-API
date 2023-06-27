package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import org.joml.Vector3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.Map;

/**
 * This Property allows to have a simple way to change the gui position and size of an item
 */
public class GuiOffsetProperty implements ModuleProperty {
    public static final String KEY = "guiOffset";
    public static ModuleProperty property;

    public GuiOffsetProperty() {
        property = this;
        ModelProperty.modelTransformers.add(new ModelProperty.ModelTransformer() {

            @Override
            public Map<String,DynamicBakedModel> bakedTransform(Map<String,DynamicBakedModel> dynamicBakedModelmap, ItemStack stack) {
                dynamicBakedModelmap.forEach((id,dynamicBakedModel)->{
                    GuiOffsetJson guiOffsetJson = new GuiOffsetJson();
                    for (ItemModule.ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(stack))) {
                        JsonElement element = instance.getProperties().get(property);
                        if (element != null) {
                            GuiOffsetJson add = Miapi.gson.fromJson(element, GuiOffsetJson.class);
                            guiOffsetJson.x += add.x;
                            guiOffsetJson.y += add.y;
                            guiOffsetJson.sizeX += add.sizeX;
                            guiOffsetJson.sizeY += add.sizeY;
                        }
                    }
                    Transform guiTransform = Transform.repair(dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.GUI));
                    guiOffsetJson.x -=guiOffsetJson.sizeX/2;
                    guiOffsetJson.y -= guiOffsetJson.sizeY/2;
                    guiOffsetJson.sizeX = guiTransform.scale.x() - guiOffsetJson.sizeX / 16.0f;
                    guiOffsetJson.sizeY = guiTransform.scale.y() - guiOffsetJson.sizeY / 16.0f;
                    float guiZ = (guiOffsetJson.sizeX + guiOffsetJson.sizeY)/2;
                    guiTransform = new Transform(guiTransform.rotation,new Vector3f(guiOffsetJson.x / 16.0f, guiOffsetJson.y / 16.0f, 0),new Vector3f(guiOffsetJson.sizeX, guiOffsetJson.sizeY, guiZ));
                    dynamicBakedModel.modelTransformation = new ModelTransformation(
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_LEFT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIRST_PERSON_LEFT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.HEAD),
                            guiTransform,
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.GROUND),
                            dynamicBakedModel.getTransformation().getTransformation(ModelTransformationMode.FIXED)
                    );
                    dynamicBakedModelmap.put(id,dynamicBakedModel);
                });
                return dynamicBakedModelmap;
            }
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    private static class GuiOffsetJson {
        public float x = 0;
        public float y = 0;
        public float sizeX = 0;
        public float sizeY = 0;
    }
}
