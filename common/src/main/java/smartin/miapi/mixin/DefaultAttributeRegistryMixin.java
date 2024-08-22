package smartin.miapi.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(DefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {

    @ModifyReturnValue(
            method = "get(Lnet/minecraft/entity/EntityType;)Lnet/minecraft/entity/attribute/DefaultAttributeContainer;",
            at = @At("RETURN"))
    private static DefaultAttributeContainer miapi$addAttributes(DefaultAttributeContainer old) {
        DefaultAttributeContainer.Builder builder = DefaultAttributeContainer.builder();
        ((DefaultAttributeContainerAccessor) old).getInstances().forEach((entityAttribute, entityAttributeInstance) -> {
            builder.add(entityAttribute, entityAttributeInstance.getValue());
        });
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(attribute, attribute.getDefaultValue());
            });
        }
        return builder.build();
    }
}
