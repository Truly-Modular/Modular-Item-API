package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.HitResult;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

/**
 * This property gives a projectile ender pearl behaviour
 */
public class EnderpearlProperty extends ComplexBooleanProperty {
    public static final String KEY = "is_enderpearl";
    public static EnderpearlProperty property;

    public EnderpearlProperty() {
        super(KEY, false);
        property = this;
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register(event -> {
            if (isEnderPearl(event.projectile)) {
                if (onCollision(event.projectile, event.entityHitResult)) {
                    return EventResult.interruptTrue();
                }
            }
            return EventResult.pass();
        });
        MiapiProjectileEvents.MODULAR_PROJECTILE_BLOCK_HIT.register(event -> {
            if (isEnderPearl(event.projectile)) {
                if (onCollision(event.projectile, event.blockHitResult)) {
                    return EventResult.interruptTrue();
                }
            }
            return EventResult.pass();
        });
    }

    /**
     * copied straigth from {@link net.minecraft.world.entity.projectile.ThrownEnderpearl}
     *
     * @param projectile the projectile in question
     * @param hitResult  the Hitresult
     * @return if the player was teleported
     */
    protected boolean onCollision(ItemProjectileEntity projectile, HitResult hitResult) {
        RandomSource random = RandomSource.create();
        for (int i = 0; i < 32; ++i) {
            projectile.level().addParticle(ParticleTypes.PORTAL, projectile.getX(), projectile.getY() + random.nextDouble() * 2.0, projectile.getZ(), random.nextGaussian(), 0.0, random.nextGaussian());
        }
        if (!projectile.level().isClientSide && !projectile.isRemoved()) {
            Entity entity = projectile.getOwner();
            if (entity instanceof ServerPlayer serverPlayerEntity) {
                if (serverPlayerEntity.connection.isAcceptingMessages() && serverPlayerEntity.level() == projectile.level() && !serverPlayerEntity.isSleeping()) {
                    Endermite endermiteEntity;
                    if (random.nextFloat() < 0.05f && projectile.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && (endermiteEntity = EntityType.ENDERMITE.create(projectile.level())) != null) {
                        endermiteEntity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                        projectile.level().addFreshEntity(endermiteEntity);
                    }
                    if (entity.isPassenger()) {
                        serverPlayerEntity.dismountTo(projectile.getX(), projectile.getY(), projectile.getZ());
                    } else {
                        entity.teleportTo(projectile.getX(), projectile.getY(), projectile.getZ());
                    }
                    entity.resetFallDistance();
                    entity.hurt(projectile.damageSources().fall(), 5.0f);
                }
            } else if (entity != null) {
                entity.teleportTo(projectile.getX(), projectile.getY(), projectile.getZ());
                entity.resetFallDistance();
            }
            projectile.discard();
            return true;
        }
        return false;
    }

    public static boolean isEnderPearl(ItemProjectileEntity projectile) {
        return isEnderPearl(projectile.getPickupItem());
    }

    public static boolean isEnderPearl(ItemStack itemStack) {
        return property.isTrue(itemStack);
    }
}
