package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import dev.architectury.event.EventResult;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.HitResult;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.mixin.ThrowablePotionItemAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property gives a projectile ender pearl behaviour
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
        ItemStack itemStack = projectile.asItemStack();
        JsonElement element = ItemModule.getMergedProperty(itemStack, property);
        if (element != null) {
            NbtCompound itemCompound = itemStack.getOrCreateNbt().getCompound(element.getAsString());
            if (!itemCompound.isEmpty()) {
                ItemStack itemStack1 = ItemStack.fromNbt(itemCompound);
                if (projectile.getOwner() instanceof LivingEntity livingEntity) {
                    if(!projectile.getWorld().isClient()){
                        if (itemStack1.getItem() instanceof ThrowablePotionItem) {
                            PotionEntity potionEntity = new PotionEntity(projectile.getWorld(), livingEntity);
                            potionEntity.setPosition(projectile.getPos());
                            potionEntity.setItem(itemStack1);
                            potionEntity.setVelocity(livingEntity, projectile.getPitch(), projectile.getYaw(), 0.0f, projectile.speed, 0.0f);
                            projectile.getWorld().spawnEntity(potionEntity);
                            ((ThrowablePotionItemAccessor) potionEntity).onCollisionMixin(hitResult);
                            projectile.discard();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }
}
