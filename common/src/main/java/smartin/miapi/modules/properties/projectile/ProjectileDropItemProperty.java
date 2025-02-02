package smartin.miapi.modules.properties.projectile;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.StackStorageComponent;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.Map;

/**
 * replaces the projectile with another projectile on impact
 */
public class ProjectileDropItemProperty extends CodecProperty<String> {
    public static final ResourceLocation KEY = Miapi.id("projectile_drop_item");
    public static ProjectileDropItemProperty property;


    public ProjectileDropItemProperty() {
        super(Codec.STRING);
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
        ItemStack itemStack = projectile.thrownStack;
        if (property.getData(itemStack).isPresent() && property.getData(itemStack).get() instanceof String path) {
            Map<String, ItemStack> map = itemStack.getOrDefault(StackStorageComponent.STACK_STORAGE_COMPONENT, Map.of());
            ItemStack storedStack = map.get(path);
            if (storedStack != null) {
                storedStack = storedStack.copy();
                if (!projectile.level().isClientSide()) {
                    projectile.level().addFreshEntity(new ItemEntity(
                            projectile.level(),
                            projectile.position().x(),
                            projectile.position().y(),
                            projectile.position().z(),
                            storedStack.copy()
                    ));
                }
                //TODO:doesnt always prevent arrow from existing!
                return true;
            }
        }
        return false;
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
