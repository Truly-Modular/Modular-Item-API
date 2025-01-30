package smartin.miapi.modules.abilities.gun;


import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.Miapi;
import smartin.miapi.network.modern.ModernNetworking;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GunMagazineComponent {
    public static final Codec<List<ItemStack>> CODEC = ItemStack.CODEC.listOf();
    public static final DataComponentType<List<ItemStack>> STACK_STORAGE_COMPONENT = DataComponentType
            .<List<ItemStack>>builder()
            .persistent(CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC))
            .build();

    public static final ResourceLocation PACKET_ID = Miapi.id("miapi", "c2s_hitscan_shot");
    public static StreamCodec<RegistryFriendlyByteBuf, Vec3> PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(Vec3.CODEC);


    static{
        ModernNetworking.registerC2SReceiver(PACKET_ID, PACKET_CODEC, (hitPos, player, access) -> {
            Level world = player.level();

            if (!world.isClientSide) {
                // Verify the hit on the server
                for (LivingEntity entity : world.getEntitiesOfClass(LivingEntity.class, new AABB(new BlockPos((int)hitPos.x,(int)hitPos.y,(int)hitPos.z)).inflate(1))) {
                    if (entity != player) {
                        //entity.hurt(DamageSource.playerAttack(player), 5.0F); // Example damage
                    }
                }
            }
        });

    }

    public static void addBullet(ItemStack gun, ItemStack bullet) {
        List<ItemStack> magazine = List.copyOf(gun.getOrDefault(STACK_STORAGE_COMPONENT, List.of()));
        magazine = magazine.stream().collect(Collectors.toList());
        magazine.add(bullet);
        gun.set(STACK_STORAGE_COMPONENT, magazine);
    }

    public static Optional<ItemStack> removeBullet(ItemStack gun) {
        List<ItemStack> magazine = List.copyOf(gun.getOrDefault(STACK_STORAGE_COMPONENT, List.of()));
        if (!magazine.isEmpty()) {
            magazine = magazine.stream().collect(Collectors.toList());
            ItemStack removed = magazine.remove(magazine.size() - 1);
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
            // Client-side hitscan logic
            if (world.isClientSide) {
                handleClientHitscan(player);
            }
        } else {
            // Server-side projectile logic
            handleProjectileShot(world, player);
        }
    }

    private static void handleClientHitscan(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(100)); // 100-block range for hitscan
        HitResult hitResult = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            // Notify the server of the hit
            ModernNetworking.sendToServer(PACKET_ID, PACKET_CODEC, hitResult.getLocation(), player.level().registryAccess());
        }
    }

    private static void handleProjectileShot(Level world, Player player) {
        //Arrow arrow = new Arrow(world, player);
        //arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F); // Similar to a bow
        //world.addFreshEntity(arrow);
    }



}
