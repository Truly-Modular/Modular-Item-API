package smartin.miapi.mixin.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ProjectileWithBow;

@Mixin(Projectile.class)
public class ProjectileMixin implements ProjectileWithBow {
    private static final String MIAPI_SHOW_BY_BOW = "miapi:show_by_bow";

    @Inject(method = "Lnet/minecraft/world/entity/projectile/Projectile;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void miapi$readBowItem(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains(MIAPI_SHOW_BY_BOW)) {
            try {
                Projectile p = (Projectile) (Object) this;
                RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, p.level().registryAccess());

                setBowItem(ItemStack.CODEC.parse(ops, compound.get(MIAPI_SHOW_BY_BOW)).resultOrPartial(msg -> {
                    Miapi.LOGGER.warn("Failed to parse bow ItemStack: {}", msg);
                }).orElse(ItemStack.EMPTY));
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("Could not decode bow stack on arrow", e);
            }
        }
    }

    @Inject(method = "Lnet/minecraft/world/entity/projectile/Projectile;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"))
    private void miapi$addBowItem(CompoundTag compound, CallbackInfo ci) {
        ItemStack miapiBowStack = getBowItem();
        if (miapiBowStack != null && !miapiBowStack.isEmpty()) {
            try {
                Projectile p = (Projectile) (Object) this;
                compound.put(MIAPI_SHOW_BY_BOW, ItemStack.CODEC.encodeStart(
                        RegistryOps.create(NbtOps.INSTANCE, p.level().registryAccess()),
                        getBowItem()
                ).getOrThrow(msg -> new RuntimeException("Failed to encode bow ItemStack: {}")));
            } catch (RuntimeException e) {
                Miapi.LOGGER.warn("Could not encode bow stack on arrow", e);
            }
        }
    }

    @Override
    @Unique
    public ItemStack getBowItem() {
        Projectile p = (Projectile) (Object) this;
        return p.getEntityData().get(ProjectileWithBow.get());
    }

    @Override
    @Unique
    public void setBowItem(ItemStack bowItem) {
        Projectile p = (Projectile) (Object) this;
        p.getEntityData().set(ProjectileWithBow.get(), bowItem);
    }
}
