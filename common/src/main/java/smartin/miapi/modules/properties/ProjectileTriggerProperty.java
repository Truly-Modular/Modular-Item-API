package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * replaces the projectile with another projectile on impact
 */
public class ProjectileTriggerProperty implements ModuleProperty {
    public static final String KEY = "replace_projectile";
    public static ProjectileTriggerProperty property;

    public ProjectileTriggerProperty() {
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
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        /*
        if (element != null && itemStack.hasNbt()) {
            CompoundTag itemCompound = itemStack.getOrCreateNbt().getCompound(element.getAsString());
            if (!itemCompound.isEmpty()) {
                ItemStack storedStack = ItemStack.parse(itemCompound);
                if (projectile.getOwner() instanceof LivingEntity livingEntity) {
                    if (!projectile.level().isClientSide()) {
                        if (storedStack.getItem() instanceof ThrowablePotionItem) {
                            ThrownPotion potionEntity = new ThrownPotion(projectile.level(), livingEntity);
                            potionEntity.setPos(projectile.position());
                            potionEntity.setItem(storedStack);
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
        }

         */
        return false;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
