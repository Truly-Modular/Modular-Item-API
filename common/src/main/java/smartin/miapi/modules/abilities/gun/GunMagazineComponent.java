package smartin.miapi.modules.abilities.gun;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static smartin.miapi.modules.abilities.gun.GunMagazineComponent.ShotPacketData.PACKET_CODEC;

public class GunMagazineComponent {
    public static final Codec<List<ItemStack>> CODEC = ItemStack.CODEC.listOf();
    public static final DataComponentType<List<ItemStack>> STACK_STORAGE_COMPONENT = DataComponentType
            .<List<ItemStack>>builder()
            .persistent(CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC))
            .build();

    public static final ResourceLocation PACKET_ID = Miapi.id("miapi", "c2s_hitscan_shot");

    static {
        ModernNetworking.registerC2SReceiver(PACKET_ID, PACKET_CODEC, (packetData, player, access) -> {
            Level world = player.level();
            ItemStack weapon = player.getWeaponItem();

            if (!world.isClientSide && (getBulletCount(weapon) > 0)) {
                double maxRange = GunContextProperty.getGunContext(weapon).range().getValue();
                Vec3 shooterPos = player.getEyePosition();

                // Validate the shot distance
                if (packetData.hitPosition.distanceTo(shooterPos) <= maxRange) {
                    packetData.hitEntityId.ifPresent(entityID -> {
                        if (player.level().getEntity(entityID) instanceof LivingEntity target && target != player) {
                            double damage = GunContextProperty.getGunContext(weapon).baseDamage().getValue();
                            var optionalBullet = removeBullet(weapon);
                            optionalBullet.ifPresent(bullet -> {
                                target.hurt(player.damageSources().playerAttack(player), (float) damage);
                            });
                        }
                    });
                }
            }
        });
    }

    public static void addBullet(ItemStack gun, ItemStack bullet) {
        List<ItemStack> magazine = List.copyOf(gun.getOrDefault(STACK_STORAGE_COMPONENT, List.of()));
        magazine = new ArrayList<>(magazine);
        magazine.add(bullet);
        gun.set(STACK_STORAGE_COMPONENT, magazine);
    }

    public static Optional<ItemStack> removeBullet(ItemStack gun) {
        List<ItemStack> magazine = List.copyOf(gun.getOrDefault(STACK_STORAGE_COMPONENT, List.of()));
        if (!magazine.isEmpty()) {
            magazine = new ArrayList<>(magazine);
            ItemStack removed = magazine.removeFirst();
            gun.set(STACK_STORAGE_COMPONENT, magazine);
            return Optional.of(removed);
        }
        return Optional.empty();
    }

    public static int getBulletCount(ItemStack gun) {
        return gun.getOrDefault(STACK_STORAGE_COMPONENT, List.of()).size();
    }

    public static void emptyMagazine(ItemStack gun) {
        gun.set(STACK_STORAGE_COMPONENT, List.of());
    }

    public static void shoot(Level world, Player player, boolean isHitscan) {
        if (isHitscan) {
            if (world.isClientSide) {
                handleClientHitscan(player);
            }
        } else {
            handleProjectileShot(world, player);
        }
    }

    private static void handleClientHitscan(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = GunContextProperty.getGunContext(player.getMainHandItem()).range().getValue();

        Vec3 end = start.add(look.scale(range));
        HitResult hitResult = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        Optional<Integer> entityHit = Optional.empty();

        if (hitResult instanceof EntityHitResult entityHitResult) {
            entityHit = Optional.of(entityHitResult.getEntity().getId());
        }

        // Send hit data to server
        ModernNetworking.sendToServer(PACKET_ID, PACKET_CODEC, new ShotPacketData(hitResult.getLocation(), entityHit), player.level().registryAccess());
    }

    private static void handleProjectileShot(Level world, Player player) {
        // Example: Spawn a bullet entity with correct properties
        // BulletEntity bullet = new BulletEntity(world, player);
        // bullet.setVelocity(player.getLookAngle().scale(3.0)); // Adjust speed
        // world.addFreshEntity(bullet);
    }

    public static class ShotPacketData {
        public final Vec3 hitPosition;
        public final Optional<Integer> hitEntityId;

        public ShotPacketData(Vec3 hitPosition, Optional<Integer> hitEntityId) {
            this.hitPosition = hitPosition;
            this.hitEntityId = hitEntityId;
        }

        public static final Codec<ShotPacketData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("hitPosition").forGetter(data -> data.hitPosition),
                Codec.INT.optionalFieldOf("hitEntityId").forGetter(data -> data.hitEntityId)
        ).apply(instance, ShotPacketData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShotPacketData> PACKET_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(CODEC);
    }
}
