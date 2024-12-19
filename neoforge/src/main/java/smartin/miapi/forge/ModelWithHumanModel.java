package smartin.miapi.forge;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class ModelWithHumanModel extends Model {

    public ModelWithHumanModel(Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
    }

    public HumanoidModel humanoidModel;
    public HumanoidModel getHumanoidModel(){
        return humanoidModel;
    }
}
