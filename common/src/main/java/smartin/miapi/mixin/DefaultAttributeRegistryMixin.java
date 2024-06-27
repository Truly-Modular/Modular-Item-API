package smartin.miapi.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.attributes.AttributeRegistry;

@Mixin(DefaultAttributes.class)
public class DefaultAttributeRegistryMixin {

    @Inject(
            method = "get(Lnet/minecraft/entity/EntityType;)Lnet/minecraft/entity/attribute/DefaultAttributeContainer;",
            at = @At("TAIL"),
            cancellable = true)
    private static void miapi$addAttributes(CallbackInfoReturnable<AttributeSupplier> cir) {
        AttributeSupplier old = cir.getReturnValue();
        AttributeSupplier.Builder builder = AttributeSupplier.builder();
        ((DefaultAttributeContainerAccessor) old).getInstances().forEach((entityAttribute, entityAttributeInstance) -> {
            builder.add(entityAttribute, entityAttributeInstance.getValue());
        });
        if (builder != null) {
            AttributeRegistry.entityAttributeMap.forEach((id, attribute) -> {
                builder.add(attribute, attribute.getDefaultValue());
            });
        }
        cir.setReturnValue(builder.build());
    }
}
