package smartin.miapi.client.model;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.resource.ResourceManager;

public class ModelLoadAccessor {
    private static ModelLoader loader;

    public static ModelLoader getLoader(){
        if(loader==null){
            ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
            loader = new ModelLoader(resourceManager,MinecraftClient.getInstance().getBlockColors(),MinecraftClient.getInstance().getProfiler(),4);
        }
        return loader;
    }
}
