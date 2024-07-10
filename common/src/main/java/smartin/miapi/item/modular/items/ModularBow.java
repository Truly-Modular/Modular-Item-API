package smartin.miapi.item.modular.items;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;

import java.util.UUID;
import java.util.function.Predicate;

public class ModularBow extends BowItem implements PlatformModularItemMethods, ModularItem {
    public static Predicate<ItemStack> projectile = ARROW_ONLY;
    public static UUID bowMoveSpeedUUId = UUID.fromString("4de85d6c-7923-11ee-b962-0242ac120002");

    public ModularBow(Properties settings) {
        super(settings.stacksTo(1).durability(50));
        if (smartin.miapi.Environment.isClient()) {
        }
    }

    public ModularBow() {
        super(new Item.Properties().stacksTo(1).durability(50));
        if (smartin.miapi.Environment.isClient()) {
        }
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemstackReferenceProvider.getFakeReference(this);
        if (itemStack != null) {
            return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
        }
        return 15;
    }

    /*
    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / ModularItem.getDurability(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float) ModularItem.getDurability(stack) - (float) stack.getDamageValue()) / ModularItem.getDurability(stack));
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return RepairPriority.getRepairValue(stack, ingredient) > 0;
    }

    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        //float movementBonus = getPullProgress(getMaxUseTime(stack) - remainingUseTicks) * 2.0f;
        //Multimap
        //Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers = ArrayListMultimap.create();
        //attributeModifiers.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(bowMoveSpeedUUId, "bowMoveSpeed", movementBonus, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        //user.getAttributes().addTemporaryModifiers(attributeModifiers);
    }

    @Override
    public void releaseUsing(ItemStack bowStack, Level world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof Player)) {
            return;
        }
        Player playerEntity = (Player) user;
        boolean consumeArrow = !playerEntity.getAbilities().instabuild;
        ItemStack projectileStack = playerEntity.getProjectile(bowStack);
        if (
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY, bowStack) > 0 &&
                (
                        projectileStack.isEmpty() ||
                        (projectileStack.getItem() instanceof ArrowItem &&
                         projectileStack.hasNbt() &&
                         projectileStack.getOrCreateNbt().isEmpty()))) {
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
        float pullProgress = getPullProgress(bowStack.getItem().getUseDuration(bowStack) - remainingUseTicks, bowStack);
        if (pullProgress < 0.1) {
            return;
        }
        shoot(bowStack, projectileStack, world, user, pullProgress, consumeArrow, playerEntity.getXRot(), playerEntity.getYRot(), 0.0f);
        if (consumeArrow) {
            projectileStack.shrink(1);
            if (projectileStack.isEmpty()) {
                playerEntity.getInventory().removeItem(projectileStack);
            }
        }
    }

    public static void shoot(ItemStack bowStack, ItemStack projectileStack, Level world, LivingEntity user, float pullProgress, boolean canPickup, float pitch, float yaw, float roll) {
        if (!(user instanceof Player)) {
            return;
        }
        Player playerEntity = (Player) user;
        if (!world.isClientSide && projectileStack.getItem() instanceof ArrowItem arrowItem) {
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH, bowStack);
            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, bowStack);
            int piercingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, bowStack);
            ItemStack projectileStackConsumed = projectileStack.copy();
            projectileStackConsumed.setCount(1);
            AbstractArrow itemProjectile = arrowItem.createArrow(world, projectileStack, playerEntity, bowStack);
            if (itemProjectile instanceof ItemProjectileEntity modularProjectile) {
                modularProjectile.setSpeedDamage(true);
            }
            itemProjectile.setPierceLevel((byte) ((byte) (int) AttributeProperty.getActualValue(projectileStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_PIERCING) + piercingLevel));

            float divergence = (float) Math.pow(12.0, -AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_ACCURACY));
            float speed = (float) Math.max(0.1, AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_SPEED) + 3.0);
            float damage = (float) AttributeProperty.getActualValue(bowStack, EquipmentSlot.MAINHAND, AttributeRegistry.PROJECTILE_DAMAGE) / speed;
            itemProjectile.setBaseDamage(damage + itemProjectile.getBaseDamage());
            itemProjectile.shootFromRotation(playerEntity, pitch, yaw, roll, pullProgress * speed, divergence);
            if (pullProgress == 1.0f) {
                itemProjectile.setCritArrow(true);
                itemProjectile.isCritArrow();
            }
            if (powerLevel > 0) {
                itemProjectile.setBaseDamage(itemProjectile.getBaseDamage() + powerLevel * 0.5 + 0.5);
            }
            if (punchLevel > 0) {
                itemProjectile.setPunch(punchLevel);
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, bowStack) > 0) {
                itemProjectile.igniteForSeconds(100);
            }
            bowStack.hurtAndBreak(1, playerEntity, p -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
            if (!canPickup) {
                itemProjectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            } else {
                itemProjectile.pickup = AbstractArrow.Pickup.ALLOWED;
            }
            MiapiProjectileEvents.ModularBowShotEvent event = new MiapiProjectileEvents.ModularBowShotEvent(itemProjectile, bowStack, playerEntity);
            if (MiapiProjectileEvents.MODULAR_BOW_SHOT.invoker().call(event).interruptsFurtherEvaluation()) {
                return;
            }
            itemProjectile = event.projectile;

            world.addFreshEntity(itemProjectile);

            MiapiProjectileEvents.ModularBowShotEvent postEvent = new MiapiProjectileEvents.ModularBowShotEvent(itemProjectile, bowStack, playerEntity);
            postEvent.bowStack = bowStack;
            postEvent.shooter = playerEntity;
            postEvent.projectile = itemProjectile;
            if (MiapiProjectileEvents.MODULAR_BOW_POST_SHOT.invoker().call(postEvent).interruptsFurtherEvaluation()) {
                return;
            }
        }
        world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + pullProgress * 0.5f);
        playerEntity.awardStat(Stats.ITEM_USED.get(bowStack.getItem()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        //BOW_PROJECTILE
        if (MiapiConfig.INSTANCE.server.enchants.betterInfinity) {
            ItemStack itemStack = user.getItemInHand(hand);
            ItemStack projectileStack = user.getProjectile(itemStack);
            if (itemStack.hasNbt()) {
                CompoundTag compound = itemStack.getOrCreateNbt();
                compound = compound.getCompound("BOW_PROJECTILE");
                projectileStack.writeNbt(compound);
            }
            boolean bl = !projectileStack.isEmpty();
            int infinityLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY, itemStack);
            if (user.getAbilities().instabuild || bl || infinityLevel > 0) {
                user.startUsingItem(hand);
                return InteractionResultHolder.consume(itemStack);
            }
            return InteractionResultHolder.fail(itemStack);
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
        ModularModelPredicateProvider.registerModelOverride(this, new ResourceLocation("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getUseItem() != stack ? 0.0F : getPullProgress((stack.getUseDuration() - entity.getUseItemRemainingTicks()), stack);
            }
        });
        ModularModelPredicateProvider.registerModelOverride(this, new ResourceLocation("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        });
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return projectile;
    }

    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, net.minecraft.world.item.Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }

     */
}
