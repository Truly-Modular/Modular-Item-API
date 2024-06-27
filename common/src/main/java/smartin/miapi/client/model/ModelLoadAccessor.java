package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.ModelBakery;

/**
 * An Accessor to a ModelLoader instance, if no ModelLoader is found it creates a new one
 * A Mixin should set this
 */
@Environment(EnvType.CLIENT)
public class ModelLoadAccessor {
    private ModelLoadAccessor(){

    }
    private static ModelBakery loader;

    public static ModelBakery getLoader(){
        return loader;
    }

    public static void setLoader(ModelBakery loader){
        ModelLoadAccessor.loader = loader;
    }
}
