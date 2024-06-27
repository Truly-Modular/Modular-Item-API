package smartin.miapi.mixin.client;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElytraModel.class)
public interface ElytraEntityModelAccessor {
    @Accessor
    ModelPart getRightWing();

    @Accessor
    ModelPart getLeftWing();
}
