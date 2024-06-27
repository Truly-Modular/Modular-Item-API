package smartin.miapi.mixin.client;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraLayer.class)
public interface ElytraFeatureRendererAccessor<T extends LivingEntity> {
    @Accessor
    ElytraModel<T> getElytra();
}
