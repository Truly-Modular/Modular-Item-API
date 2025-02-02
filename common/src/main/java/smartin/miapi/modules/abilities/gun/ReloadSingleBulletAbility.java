package smartin.miapi.modules.abilities.gun;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.util.CodecAbility;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;

import java.util.Optional;
import java.util.function.Predicate;

public class ReloadSingleBulletAbility implements CodecAbility<ReloadSingleBulletAbility.ReloadAbilityContext> {
    public static ResourceLocation KEY = Miapi.id("gun_reload_single");

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        boolean hasSpace = GunMagazineComponent.getBulletCount(itemStack) < GunContextProperty.getGunContext(itemStack).magazineSize().getValue();
        boolean hasAbility = getSpecialContext(itemStack) != null;
        return hasAbility && hasSpace;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return (int) Math.ceil(getSpecialContext(itemStack).reloadDelaySeconds * 20);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        ReloadAbilityContext context = getSpecialContext(gun);

        if (context == null || GunMagazineComponent.getBulletCount(gun) >= GunContextProperty.getGunContext(gun).magazineSize().getValue()) {
            return InteractionResultHolder.pass(gun);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(gun);
    }

    @Override
    public void onStoppedUsing(ItemStack gun, Level world, LivingEntity entity, int timeCharged) {
        if (!(entity instanceof Player player)) return;

        ReloadAbilityContext context = getSpecialContext(gun);
        if (context == null) return;

        int chargeDuration = getMaxUseTime(gun, entity) - timeCharged;
        if (chargeDuration < getMaxUseTime(gun, entity)) return; // Reload must be fully charged

        Optional<ItemStack> bullet = player.getInventory().items.stream()
                .filter(context.bulletFilter)
                .findFirst();

        if (bullet.isEmpty()) return;

        // Remove a bullet from inventory and add it to the magazine
        bullet.get().shrink(1);
        GunMagazineComponent.addBullet(gun, new ItemStack(bullet.get().getItem()));

        // Play reload sound
        if (context.reloadSound != null) {
            SoundEvent event = world.registryAccess().registry(Registries.SOUND_EVENT).get().get(context.reloadSound);
            if (event != null) {
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), event, entity.getSoundSource(), 1.0f, 1.0f);
            }
        }

        // Play reload animation
        if (context.reloadAnim != null) {
            setAnimation(player, InteractionHand.MAIN_HAND, context.reloadAnim);
            world.getServer().execute(() -> resetAnimation(player)); // Reset animation after reload completes
        }
    }

    private void setAnimation(Player player, InteractionHand hand, ResourceLocation location) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(serverPlayer);
            if (facet != null) {
                facet.set(location.toString(), serverPlayer, hand);
            }
        }
    }

    private void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(serverPlayer);
            if (facet != null) {
                facet.reset(serverPlayer);
            }
        }
    }

    @Override
    public Codec<ReloadAbilityContext> getCodec() {
        return ReloadAbilityContext.CODEC;
    }

    public static class ReloadAbilityContext {
        public static final Codec<ReloadAbilityContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("reloadDelaySeconds").forGetter(context -> context.reloadDelaySeconds),
                ResourceLocation.CODEC.optionalFieldOf("reloadAnim").forGetter(context -> Optional.ofNullable(context.reloadAnim)),
                ResourceLocation.CODEC.optionalFieldOf("reloadSound").forGetter(context -> Optional.ofNullable(context.reloadSound))
        ).apply(instance, ReloadAbilityContext::new));

        public final float reloadDelaySeconds;
        public final Predicate<ItemStack> bulletFilter;
        public final ResourceLocation reloadAnim;
        public final ResourceLocation reloadSound;

        public ReloadAbilityContext(float reloadDelaySeconds, Optional<ResourceLocation> reloadAnim, Optional<ResourceLocation> reloadSound) {
            this.reloadDelaySeconds = reloadDelaySeconds;
            this.bulletFilter = (i) -> {
                return true;
            };
            this.reloadAnim = reloadAnim.orElse(null);
            this.reloadSound = reloadSound.orElse(null);
        }
    }
}
