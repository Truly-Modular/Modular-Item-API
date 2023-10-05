package smartin.miapi.mixin.client;

import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraFeatureRenderer.class)
public interface ElytraFeatureRendererAccessor<T extends LivingEntity> {
    @Accessor
    ElytraEntityModel<T> getElytra();
}
