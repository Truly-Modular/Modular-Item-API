package smartin.miapi.item.modular;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.AbilityProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.HashMap;

public class ItemAbilityManager {
    private static final HashMap<PlayerEntity, ItemStack> playerActiveItems = new HashMap<>();
    private static final HashMap<PlayerEntity, ItemStack> playerActiveItemsClient = new HashMap<>();
    public static final MiapiRegistry<ItemUseAbility> useAbilityRegistry = MiapiRegistry.getInstance(ItemUseAbility.class);
    private static EmptyAbility emptyAbility;

    public static void setup() {
        TickEvent.PLAYER_PRE.register((playerEntity) -> {
            if(playerEntity instanceof ServerPlayerEntity){
                ItemStack oldItem = playerActiveItems.get(playerEntity);
                ItemStack playerItem = playerEntity.getActiveItem();
                if (playerItem != null && !playerItem.equals(oldItem)) {
                    playerActiveItems.put(playerEntity, playerEntity.getActiveItem());
                    if(oldItem!=null){
                        getAbility(oldItem).onStoppedHolding(oldItem, playerEntity.world, playerEntity);
                    }
                }
            }
            else{
                ItemStack oldItem = playerActiveItemsClient.get(playerEntity);
                ItemStack playerItem = playerEntity.getActiveItem();
                if (playerItem != null && !playerItem.equals(oldItem)) {
                    playerActiveItemsClient.put(playerEntity, playerEntity.getActiveItem());
                    if(oldItem!=null){
                        getAbility(oldItem).onStoppedHolding(oldItem, playerEntity.world, playerEntity);
                    }
                }
            }
        });
        emptyAbility = new EmptyAbility();
        useAbilityRegistry.register("empty", emptyAbility);
    }

    public static ItemUseAbility getEmpty() {
        return emptyAbility;
    }

    private static ItemUseAbility getAbility(ItemStack itemStack) {
        for (ItemUseAbility ability : AbilityProperty.get(itemStack)) {
            if (ability.allowedOnItem(itemStack)) {
                return ability;
            }
        }
        return emptyAbility;
    }

    public static UseAction getUseAction(ItemStack itemStack) {
        return getAbility(itemStack).getUseAction(itemStack);
    }

    public static int getMaxUseTime(ItemStack itemStack) {
        return getAbility(itemStack).getMaxUseTime(itemStack);
    }

    public static TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return getAbility(user.getStackInHand(hand)).use(world, user, hand);
    }

    public static UseAction onUse(World world, PlayerEntity user, Hand hand) {
        return getAbility(user.getStackInHand(hand)).onUse(world, user, hand);
    }

    public static ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return getAbility(stack).finishUsing(stack, world, user);
    }

    public static void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        getAbility(stack).onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    public static void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {
        getAbility(stack).onStoppedHolding(stack, world, user);
    }

    public static ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return getAbility(stack).useOnEntity(stack, user, entity, hand);
    }

    public static ActionResult useOnBlock(ItemUsageContext context) {
        return getAbility(context.getStack()).useOnBlock(context);
    }

    static class EmptyAbility implements ItemUseAbility {

        @Override
        public boolean allowedOnItem(ItemStack itemStack) {
            return true;
        }

        @Override
        public UseAction getUseAction(ItemStack itemStack) {
            return UseAction.NONE;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack) {
            return 0;
        }

        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        @Override
        public UseAction onUse(World world, PlayerEntity user, Hand hand) {
            return UseAction.NONE;
        }

        @Override
        public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
            return stack;
        }

        @Override
        public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {

        }

        @Override
        public void onStoppedHolding(ItemStack stack, World world, LivingEntity user) {

        }

        @Override
        public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
            return ActionResult.PASS;
        }

        @Override
        public ActionResult useOnBlock(ItemUsageContext context) {
            return ActionResult.PASS;
        }
    }
}
