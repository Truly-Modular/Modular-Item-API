package smartin.miapi.mixin.client;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.class)
public interface ModelLoaderInterfaceAccessor {

    @Invoker("loadModelFromJson")
    BlockModel loadModelFromPath(ResourceLocation identifier);

}
