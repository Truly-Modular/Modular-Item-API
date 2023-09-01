package smartin.miapi.mixin;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(DefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {

    @Inject(
            method = "get(Lnet/minecraft/entity/EntityType;)Lnet/minecraft/entity/attribute/DefaultAttributeContainer;",
            at = @At("TAIL"),
            cancellable = true)
    private static void miapi$addAttributes(CallbackInfoReturnable<DefaultAttributeContainer> cir) {
        DefaultAttributeContainer old = cir.getReturnValue();
        DefaultAttributeContainer.Builder builder = DefaultAttributeContainer.builder();
        ((DefaultAttributeContainerAccessor) old).getInstances().forEach((entityAttribute, entityAttributeInstance) -> {
            builder.add(entityAttribute, entityAttributeInstance.getValue());
        });
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(attribute);
            });
        }
        cir.setReturnValue(builder.build());
    }
}
