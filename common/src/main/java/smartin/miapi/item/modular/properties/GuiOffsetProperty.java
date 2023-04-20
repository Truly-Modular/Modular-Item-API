package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.item.modular.properties.render.ModelProperty;

public class GuiOffsetProperty implements ModuleProperty {
    public static final String key = "guiOffset";
    public static ModuleProperty guiOffsetProperty;

    public GuiOffsetProperty() {
        guiOffsetProperty = this;
        ModelProperty.modelTransformers.add(new ModelProperty.ModelTransformer() {

            @Override
            public DynamicBakedModel transform(DynamicBakedModel dynamicBakedModel, ItemStack stack) {
                GuiOffsetJson guiOffsetJson = new GuiOffsetJson();
                for (ItemModule.ModuleInstance instance : ItemModule.createFlatList(ModularItem.getModules(stack))) {
                    JsonElement element = instance.getProperties().get(guiOffsetProperty);
                    if (element != null) {
                        GuiOffsetJson add = Miapi.gson.fromJson(element, GuiOffsetJson.class);
                        guiOffsetJson.x += add.x;
                        guiOffsetJson.y += add.y;
                        guiOffsetJson.sizeX += add.sizeX;
                        guiOffsetJson.sizeY += add.sizeY;
                    }
                }
                Transform guiTransform = Transform.repair(dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.GUI));
                guiOffsetJson.x -=guiOffsetJson.sizeX/2;
                guiOffsetJson.y -= guiOffsetJson.sizeY/2;
                guiOffsetJson.sizeX = guiTransform.scale.getX() - guiOffsetJson.sizeX / 16.0f;
                guiOffsetJson.sizeY = guiTransform.scale.getY() - guiOffsetJson.sizeY / 16.0f;
                guiTransform = new Transform(guiTransform.rotation,new Vec3f(guiOffsetJson.x / 16.0f, guiOffsetJson.y / 16.0f, 0),new Vec3f(guiOffsetJson.sizeX, guiOffsetJson.sizeY, 1.0f));
                ModelTransformation transformation = new ModelTransformation(
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.THIRD_PERSON_LEFT_HAND),
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.THIRD_PERSON_RIGHT_HAND),
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.FIRST_PERSON_LEFT_HAND),
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.FIRST_PERSON_RIGHT_HAND),
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.HEAD),
                        guiTransform,
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.GROUND),
                        dynamicBakedModel.getTransformation().getTransformation(ModelTransformation.Mode.FIXED)
                );
                dynamicBakedModel.modelTransformation = transformation;
                return dynamicBakedModel;
            }
        });
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    private class GuiOffsetJson {
        public float x = 0;
        public float y = 0;
        public float sizeX = 0;
        public float sizeY = 0;
    }
}
