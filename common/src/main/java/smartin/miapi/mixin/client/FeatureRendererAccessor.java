package smartin.miapi.mixin.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.class)
public interface FeatureRendererAccessor<T extends Entity, M extends EntityModel<T>> {
    @Accessor
    RenderLayerParent<T, M> getContext();
}
