package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelLoader;

/**
 * An Accessor to a ModelLoader instance, if no ModelLoader is found it creates a new one
 * A Mixin should set this
 */
@Environment(EnvType.CLIENT)
public class ModelLoadAccessor {
    private ModelLoadAccessor(){

    }
    private static ModelLoader loader;

    public static ModelLoader getLoader(){
        return loader;
    }

    public static void setLoader(ModelLoader loader){
        ModelLoadAccessor.loader = loader;
    }
}
