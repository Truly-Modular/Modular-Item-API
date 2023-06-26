package smartin.miapi.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.architectury.event.EventResult;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.miapi.events.Event;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.EquipmentSlotProperty;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    //LivingEntityMixin(final EntityType<?> type, final World world) {
    //    super(type, world);
    //}

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("TAIL"), cancellable = false)
    private void constructor(EntityType entityType, World world, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        Multimap<EntityAttribute, EntityAttributeModifier> map = HashMultimap.create();
        map.put(AttributeRegistry.BACK_STAB, new EntityAttributeModifier("default", 1, EntityAttributeModifier.Operation.ADDITION));
        entity.getAttributes().addTemporaryModifiers(map);
    }

    @Inject(method = "getPreferredEquipmentSlot", at = @At("HEAD"), cancellable = true)
    private static void onGetPreferredEquipmentSlot(ItemStack stack, CallbackInfoReturnable<EquipmentSlot> cir) {
        if (stack.getItem() instanceof ModularItem) {
            EquipmentSlot slot = EquipmentSlotProperty.getSlot(stack);
            if (slot != null) {
                cir.setReturnValue(slot);
            }
        }
    }

    @Inject(method = "createLivingAttributes", at = @At("TAIL"), cancellable = true)
    private static void addAttributes(CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
        DefaultAttributeContainer.Builder builder = cir.getReturnValue();
        if (builder != null) {
            AttributeRegistry.entityAttributeRegistry.getFlatMap().forEach((id, attribute)->{
                builder
                        .add(attribute);
            });
        }
    }

    private float storedValue;
    private DamageSource storedDamageSource;

    @Inject(method = "damage", at = @At(value = "HEAD"))
    private void damageEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Event.LivingHurtEvent livingHurtEvent = new Event.LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        EventResult result = Event.LIVING_HURT.invoker().hurt(livingHurtEvent);
        if (result.interruptsFurtherEvaluation()) {
            cir.setReturnValue(false);
        }
        storedValue = livingHurtEvent.amount;
        storedDamageSource = livingHurtEvent.damageSource;

    }

    @Inject(method = "damage", at = @At(value = "TAIL"))
    private void damageEventAfter(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Event.LivingHurtEvent livingHurtEvent = new Event.LivingHurtEvent((LivingEntity) (Object) this, source, amount);
        Event.LIVING_HURT_AFTER.invoker().hurt(livingHurtEvent);
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), ordinal = 0)
    private float damageEventValue(float value) {
        return storedValue;
    }

    @ModifyVariable(method = "damage", at = @At(value = "HEAD"), ordinal = 0)
    private DamageSource damageEventSource(DamageSource value) {
        return storedDamageSource;
    }
}
