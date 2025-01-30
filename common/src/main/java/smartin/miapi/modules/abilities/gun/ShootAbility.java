package smartin.miapi.modules.abilities.gun;

import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;

public class ShootAbility implements ItemUseAbility<ShootAbility.GunAbilityContext> {

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        return getBulletCount(itemStack) > 0;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity livingEntity) {
        return 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        GunAbilityContext context = getSpecialContext(itemStack);

        if (context == null || getBulletCount(itemStack) <= 0) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (!world.isClientSide) {
            performShooting(world, user, itemStack, context);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public <K> GunAbilityContext decode(DynamicOps<K> ops, K prefix) {
        return null;
    }

    private void performShooting(Level world, Player user, ItemStack itemStack, GunAbilityContext context) {
        // Reduce bullet count
        decreaseBulletCount(itemStack);

        // Play shooting sound
        if (context.onShoot != null) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(), context.onShoot, user.getSoundSource(), 1.0f, 1.0f);
        }

        // Trigger animation
        if (context.shotAnim != null) {
            triggerAnimation(user, context.shotAnim);
        }
    }

    private void triggerAnimation(Player user, ResourceLocation anim) {
        System.out.printf("Triggered animation: %s for player: %s\n", anim, user.getName().getString());
    }

    public static int getBulletCount(ItemStack gun) {
        // Implementation based on the magazine system
        return 10; // Placeholder value
    }

    public static void decreaseBulletCount(ItemStack gun) {
        // Logic to reduce bullet count
    }

    @Override
    public GunAbilityContext getDefaultContext() {
        return new GunAbilityContext();
    }

    public static class GunAbilityContext {
        public SoundEvent onShoot;
        public ResourceLocation shotAnim;
    }
}
