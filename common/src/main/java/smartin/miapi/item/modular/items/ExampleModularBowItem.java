package smartin.miapi.item.modular.items;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.DisplayNameProperty;

import java.util.function.Predicate;

public class ExampleModularBowItem extends BowItem implements ModularItem {
    public static boolean betterInfinity = true;

    public ExampleModularBowItem() {
        super(new Item.Settings());
        if (smartin.miapi.Environment.isClient()) {
            registerAnimations();
        }
    }

    @Override
    public void onStoppedUsing(ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity playerEntity = (PlayerEntity) user;
        boolean consumeArrow = !playerEntity.getAbilities().creativeMode;
        ItemStack projectileStack = playerEntity.getProjectileType(bowStack);
        if(EnchantmentHelper.getLevel(Enchantments.INFINITY, bowStack) > 0 &&(projectileStack.isEmpty() || (projectileStack.getItem() instanceof ArrowItem && projectileStack.getOrCreateNbt().isEmpty()))){
            consumeArrow = false;
            projectileStack = new ItemStack(Items.ARROW);
        }
        if (projectileStack.isEmpty() && consumeArrow && !betterInfinity) {
            return;
        }
        if (projectileStack.isEmpty()) {
            projectileStack = new ItemStack(Items.ARROW);
        }
        float pullProgress = getPullProgress(this.getMaxUseTime(bowStack) - remainingUseTicks, bowStack);
        if (pullProgress < 0.1) {
            return;
        }
        if (!world.isClient) {
            int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, bowStack);
            int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, bowStack);
            int piercingLevel = EnchantmentHelper.getLevel(Enchantments.PIERCING, bowStack);
            ItemStack projectileStackConsumed = projectileStack.copy();
            projectileStackConsumed.setCount(1);
            ItemProjectile itemProjectile = new ItemProjectile(world, playerEntity, projectileStackConsumed);
            itemProjectile.setBowItem(bowStack);
            itemProjectile.setPierceLevel((byte) ((byte) (int) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING) + piercingLevel));

            float divergence = (float) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY);
            float speed = (float) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
            float damage = (float) AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE);
            divergence += (float) AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY);
            speed += (float) AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED);
            damage += (float) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE);
            damage = damage / speed;
            itemProjectile.setDamage(damage);
            itemProjectile.setSpeedDamage(true);
            itemProjectile.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f, pullProgress * speed, divergence);
            if (pullProgress == 1.0f) {
                itemProjectile.setCritical(true);
            }
            if (powerLevel > 0) {
                itemProjectile.setDamage(itemProjectile.getDamage() + (double) powerLevel * 0.5 + 0.5);
            }
            if (punchLevel > 0) {
                itemProjectile.setPunch(punchLevel);
            }
            if (EnchantmentHelper.getLevel(Enchantments.FLAME, bowStack) > 0) {
                itemProjectile.setOnFireFor(100);
            }
            bowStack.damage(1, playerEntity, p -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
            if (!consumeArrow || (projectileStack.isOf(Items.SPECTRAL_ARROW) || projectileStack.isOf(Items.TIPPED_ARROW))) {
                itemProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }
            world.spawnEntity(itemProjectile);
        }
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f);
        if (consumeArrow) {
            projectileStack.decrement(1);
            if (projectileStack.isEmpty()) {
                playerEntity.getInventory().removeOne(projectileStack);
            }
        }
        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (betterInfinity) {
            ItemStack itemStack = user.getStackInHand(hand);
            boolean bl = !user.getProjectileType(itemStack).isEmpty();
            int infinityLevel = EnchantmentHelper.getLevel(Enchantments.INFINITY, itemStack);
            if (user.getAbilities().creativeMode || bl || infinityLevel > 0) {
                user.setCurrentHand(hand);
                return TypedActionResult.consume(itemStack);
            }
            return TypedActionResult.fail(itemStack);
        } else {
            return super.use(world, user, hand);
        }
    }

    public static float getPullProgress(int useTicks, ItemStack stack) {
        float f = (float) ((float) useTicks / AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.BOW_DRAW_TIME));
        if ((f = (f * f + f * 2.0f) / 3.0f) > 1.0f) {
            f = 1.0f;
        }
        return f;
    }

    @Environment(EnvType.CLIENT)
    public void registerAnimations() {
        ModularModelPredicateProvider.registerModelOverride(this, new Identifier("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItem() != stack ? 0.0F : (float) (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20.0F;
            }
        });
        ModularModelPredicateProvider.registerModelOverride(this, new Identifier("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
        });
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return BOW_PROJECTILES;
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
