package smartin.miapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

@Environment(EnvType.CLIENT)
public class MiapiClient{

    public static void setupClient(){
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new CustomModelRegistry());
    }
}
