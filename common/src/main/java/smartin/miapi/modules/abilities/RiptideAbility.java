package smartin.miapi.modules.abilities;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseAbility;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;
import smartin.miapi.modules.properties.RiptideProperty;

/**
 * This Ability allows you to use the Trident riptide Effect
 */
public class RiptideAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        RiptideProperty.RiptideJson json = RiptideProperty.getData(itemStack);
        if (json == null) return false;
        boolean missingWater = !player.isInWaterOrRain();
        boolean missingLava = json.allowLava && !player.isInLava();
        if (json.needsWater && (missingWater && missingLava)) {
            return false;
        }
        if (json.needRiptideEnchant && EnchantmentHelper.getRiptide(itemStack) <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 7200;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (itemStack.getDamageValue() >= itemStack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(itemStack);
        } else if (EnchantmentHelper.getRiptide(itemStack) > 0 && !user.isInWaterOrRain()) {
            return InteractionResultHolder.fail(itemStack);
        } else {
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(itemStack);
        }
    }

    @Override
    public int minHoldTimeDefault(){
        return 10;
    }

    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof Player playerEntity) {
            int i = this.getMaxUseTime(stack) - remainingUseTicks;
            if (i >= 10) {
                int j = EnchantmentHelper.getRiptide(stack);
                RiptideProperty.RiptideJson json = RiptideProperty.getData(stack);

                playerEntity.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                float f = playerEntity.getYRot();
                float g = playerEntity.getXRot();
                float h = -Mth.sin(f * 0.017453292F) * Mth.cos(g * 0.017453292F);
                float k = -Mth.sin(g * 0.017453292F);
                float l = Mth.cos(f * 0.017453292F) * Mth.cos(g * 0.017453292F);
                float m = Mth.sqrt(h * h + k * k + l * l);
                float n = (float) (json.riptideStrength * ((1.0F + j) / 4.0F));
                h *= n / m;
                k *= n / m;
                l *= n / m;
                playerEntity.push(h, k, l);
                playerEntity.startAutoSpinAttack(20);
                if (playerEntity.onGround()) {
                    playerEntity.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                }

                SoundEvent soundEvent;
                if (j >= 3) {
                    soundEvent = SoundEvents.TRIDENT_RIPTIDE_3;
                } else if (j == 2) {
                    soundEvent = SoundEvents.TRIDENT_RIPTIDE_2;
                } else {
                    soundEvent = SoundEvents.TRIDENT_RIPTIDE_1;
                }
                world.playSound((Player) null, playerEntity, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}
