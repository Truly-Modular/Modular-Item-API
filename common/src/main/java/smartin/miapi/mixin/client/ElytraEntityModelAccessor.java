package smartin.miapi.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraEntityModel.class)
public interface ElytraEntityModelAccessor {
    @Accessor
    ModelPart getRightWing();

    @Accessor
    ModelPart getLeftWing();
}
