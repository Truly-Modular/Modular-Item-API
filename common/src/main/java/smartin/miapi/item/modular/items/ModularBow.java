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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.client.model.ModularModelPredicateProvider;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.RarityProperty;
import smartin.miapi.modules.properties.RepairPriority;

import java.util.UUID;
import java.util.function.Predicate;

public class ModularBow extends BowItem implements ModularItem {
    public static Predicate<ItemStack> projectile = BOW_PROJECTILES;
    public static UUID bowMoveSpeedUUId = UUID.fromString("4de85d6c-7923-11ee-b962-0242ac120002");

    public ModularBow() {
        super(new Item.Settings().maxCount(1).maxDamage(50));
        if (smartin.miapi.Environment.isClient()) {
            registerAnimations();
        }
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 1;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamage() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamage()) / ModularItem.getDurability(stack));
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
    }

    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        //float movementBonus = getPullProgress(getMaxUseTime(stack) - remainingUseTicks) * 2.0f;
        //Multimap
        //Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = ArrayListMultimap.create();
        //attributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(bowMoveSpeedUUId, "bowMoveSpeed", movementBonus, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        //user.getAttributes().addTemporaryModifiers(attributeModifiers);
    }

    @Override
    public void onStoppedUsing(ItemStack bowStack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity playerEntity = (PlayerEntity) user;
        boolean consumeArrow = !playerEntity.getAbilities().creativeMode;
        ItemStack projectileStack = playerEntity.getProjectileType(bowStack);
        if (EnchantmentHelper.getLevel(Enchantments.INFINITY, bowStack) > 0 && (projectileStack.isEmpty() || (projectileStack.getItem() instanceof ArrowItem && projectileStack.getOrCreateNbt().isEmpty()))) {
            if (projectileStack.isEmpty() || projectileStack.getItem() == Items.ARROW) {
                consumeArrow = false;
                projectileStack = new ItemStack(Items.ARROW);
                projectileStack.setCount(1);
            }
        }
        if (projectileStack.isEmpty() && consumeArrow && !MiapiConfig.INSTANCE.server.enchants.betterInfinity) {
            return;
        }
        if (projectileStack.isEmpty()) {
            projectileStack = new ItemStack(Items.ARROW);
        }
        float pullProgress = getPullProgress(bowStack.getItem().getMaxUseTime(bowStack) - remainingUseTicks, bowStack);
        if (pullProgress < 0.1) {
            return;
        }
        shoot(bowStack, projectileStack, world, user, pullProgress, !consumeArrow, playerEntity.getPitch(), playerEntity.getYaw(), 0.0f);
        if (consumeArrow) {
            projectileStack.decrement(1);
            if (projectileStack.isEmpty()) {
                playerEntity.getInventory().removeOne(projectileStack);
            }
        }
    }

    public static void shoot(ItemStack bowStack, ItemStack projectileStack, World world, LivingEntity user, float pullProgress, boolean canPickup, float pitch, float yaw, float roll) {
        if (!(user instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity playerEntity = (PlayerEntity) user;
        if (!world.isClient && projectileStack.getItem() instanceof ArrowItem arrowItem) {
            int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, bowStack);
            int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, bowStack);
            int piercingLevel = EnchantmentHelper.getLevel(Enchantments.PIERCING, bowStack);
            ItemStack projectileStackConsumed = projectileStack.copy();
            projectileStackConsumed.setCount(1);
            PersistentProjectileEntity itemProjectile = arrowItem.createArrow(world, projectileStack, playerEntity);
            if (itemProjectile instanceof ItemProjectileEntity modularProjectile) {
                modularProjectile.setSpeedDamage(true);
            }
            itemProjectile.setPierceLevel((byte) ((byte) (int) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING) + piercingLevel));

            float divergence = (float) Math.pow(12.0, -AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY));
            float speed = (float) Math.max(0.1, AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED) + 3.0);
            float damage = (float) AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE) / speed;
            itemProjectile.setDamage(damage + itemProjectile.getDamage());
            itemProjectile.setVelocity(playerEntity, pitch, yaw, roll, pullProgress * speed, divergence);
            if (pullProgress == 1.0f) {
                itemProjectile.setCritical(true);
                itemProjectile.isCritical();
            }
            if (powerLevel > 0) {
                itemProjectile.setDamage(itemProjectile.getDamage() + powerLevel * 0.5 + 0.5);
            }
            if (punchLevel > 0) {
                itemProjectile.setPunch(punchLevel);
            }
            if (EnchantmentHelper.getLevel(Enchantments.FLAME, bowStack) > 0) {
                itemProjectile.setOnFireFor(100);
            }
            bowStack.damage(1, playerEntity, p -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
            if (!canPickup) {
                itemProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            } else {
                itemProjectile.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            }
            MiapiProjectileEvents.ModularBowShotEvent event = new MiapiProjectileEvents.ModularBowShotEvent(itemProjectile, bowStack, playerEntity);
            if (MiapiProjectileEvents.MODULAR_BOW_SHOT.invoker().call(event).interruptsFurtherEvaluation()) {
                return;
            }
            itemProjectile = event.projectile;

            world.spawnEntity(itemProjectile);

            MiapiProjectileEvents.ModularBowShotEvent postEvent = new MiapiProjectileEvents.ModularBowShotEvent(itemProjectile, bowStack, playerEntity);
            postEvent.bowStack = bowStack;
            postEvent.shooter = playerEntity;
            postEvent.projectile = itemProjectile;
            if (MiapiProjectileEvents.MODULAR_BOW_POST_SHOT.invoker().call(postEvent).interruptsFurtherEvaluation()) {
                return;
            }
        }
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f);
        playerEntity.incrementStat(Stats.USED.getOrCreateStat(bowStack.getItem()));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        //BOW_PROJECTILE
        if (MiapiConfig.INSTANCE.server.enchants.betterInfinity) {
            ItemStack itemStack = user.getStackInHand(hand);
            ItemStack projectileStack = user.getProjectileType(itemStack);
            NbtCompound compound = itemStack.getOrCreateNbt();
            compound = compound.getCompound("BOW_PROJECTILE");
            projectileStack.writeNbt(compound);
            boolean bl = !projectileStack.isEmpty();
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
        if (useTicks < 1) {
            return 0;
        }
        float f = (float) ((float) useTicks / (20 - AttributeProperty.getActualValue(stack, EquipmentSlot.MAINHAND, AttributeRegistry.BOW_DRAW_TIME)));
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
                return entity.getActiveItem() != stack ? 0.0F : getPullProgress((stack.getMaxUseTime() - entity.getItemUseTimeLeft()), stack);
            }
        });
        ModularModelPredicateProvider.registerModelOverride(this, new Identifier("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
        });
    }

    @Override
    public Predicate<ItemStack> getProjectiles() {
        return projectile;
    }

    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
