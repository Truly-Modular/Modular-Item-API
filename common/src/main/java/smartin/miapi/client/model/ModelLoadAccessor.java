package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;

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
        if(loader==null){
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            //loader = new ModelLoader(resourceManager,MinecraftClient.getInstance().getBlockColors(),MinecraftClient.getInstance().getProfiler(),4);
        }
        return loader;
    }

    public static void setLoader(ModelLoader loader){
        ModelLoadAccessor.loader = loader;
    }
}
