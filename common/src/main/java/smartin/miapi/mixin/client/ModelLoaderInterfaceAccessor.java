package smartin.miapi.mixin.client;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelLoader.class)
public interface ModelLoaderInterfaceAccessor {

    @Invoker("loadModelFromJson")
    JsonUnbakedModel loadModelFromPath(Identifier identifier);
}
