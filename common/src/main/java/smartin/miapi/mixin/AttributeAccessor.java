package smartin.miapi.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Attribute.class)
public interface AttributeAccessor {
    @Accessor("sentiment")
    Attribute.Sentiment getMiapiSentiment();
}
