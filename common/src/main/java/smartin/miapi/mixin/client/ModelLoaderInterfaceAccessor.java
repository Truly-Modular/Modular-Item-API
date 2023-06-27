package smartin.miapi.mixin.client;

import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ModelLoader.class)
public interface ModelLoaderInterfaceAccessor {

    @Invoker("loadModelFromJson")
    JsonUnbakedModel loadModelFromPath(Identifier identifier);

}
