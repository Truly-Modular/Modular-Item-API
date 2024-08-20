package smartin.miapi.modules.properties.projectile;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.phys.HitResult;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.ThrowablePotionItemAccessor;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Optional;

/**
 * replaces the projectile with another projectile on impact
 */
public class ProjectileTriggerProperty extends CodecProperty<ItemStack> {
    public static final ResourceLocation KEY = Miapi.id("replace_projectile");
    public static ProjectileTriggerProperty property;


    public ProjectileTriggerProperty() {
        super(ItemStack.CODEC);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            if (isTriggered(event.projectile, event.entityHitResult)) {
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            if (isTriggered(event.projectile, event.blockHitResult)) {
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });
    }

    public static boolean isTriggered(ItemProjectileEntity projectile, HitResult hitResult) {
        ItemStack itemStack = projectile.getPickupItem();
        Optional<ItemStack> storedStack = property.getData(itemStack);
        if (storedStack.isPresent()) {
            if (projectile.getOwner() instanceof LivingEntity livingEntity) {
                if (!projectile.level().isClientSide()) {
                    if (storedStack.get().getItem() instanceof ThrowablePotionItem) {
                        ThrownPotion potionEntity = new ThrownPotion(projectile.level(), livingEntity);
                        potionEntity.setPos(projectile.position());
                        potionEntity.setItem(storedStack.get());
                        potionEntity.shootFromRotation(livingEntity, projectile.getXRot(), projectile.getYRot(), 0.0f, projectile.flyDist, 0.0f);
                        projectile.level().addFreshEntity(potionEntity);
                        ((ThrowablePotionItemAccessor) potionEntity).onCollisionMixin(hitResult);
                        projectile.discard();
                        return true;
                    }
                }
                //TODO:the fuck was this suposed todo?
                //DispenseItemBehavior dispenserBehavior = DispenserBlockAccessor.getBehaviours().get(storedStack.getItem());
                //if (dispenserBehavior instanceof ProjectileDispenseBehavior projectileDispenserBehavior) {
                //    Projectile projectileEntity = ((ProjectileDispenserBehaviorAccessor) projectileDispenserBehavior).createProjectileAccessor(projectile.level(), projectile.position(), storedStack);
                //    ((ProjectileEntityAccessor) projectileEntity).onCollisionMixin(hitResult);
                //}
            }
        }
        return false;
    }

    @Override
    public ItemStack merge(ItemStack left, ItemStack right, MergeType mergeType) {
        return right;
    }
}
