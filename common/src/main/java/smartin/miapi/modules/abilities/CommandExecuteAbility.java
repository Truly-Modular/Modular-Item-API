package smartin.miapi.modules.abilities;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * This Ability executes configured commands when used.
 */
public class CommandExecuteAbility implements
        ItemUseDefaultCooldownAbility<CommandExecuteAbility.CommandExecuteJson>,
        ItemUseMinHoldAbility<CommandExecuteAbility.CommandExecuteJson> {

    public static String KEY = "command";

    public static Codec<CommandExecuteJson> CODEC = AutoCodec.of(CommandExecuteJson.class).codec();

    @Override
    public boolean allowedOnItem(ItemStack itemStack, Level world, Player player, InteractionHand hand,
                                 ItemAbilityManager.AbilityHitContext abilityHitContext, CommandExecuteJson context) {
        return true;
    }

    @Override
    public UseAnim getUseAction(ItemStack itemStack, CommandExecuteJson context) {
        return context.userAnim;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack, LivingEntity entity, CommandExecuteJson context) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand, CommandExecuteJson context) {
        if (user.getCooldowns().isOnCooldown(user.getItemInHand(hand).getItem())) {
            return InteractionResultHolder.pass(user.getItemInHand(hand));
        }
        user.startUsingItem(hand);

        if (context.startPose != null && user instanceof ServerPlayer serverPlayer) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(serverPlayer);
            if (facet != null) {
                facet.set(context.startPose, serverPlayer, hand);
            }
        }
        return InteractionResultHolder.consume(user.getItemInHand(hand));
    }

    @Override
    public Codec<CommandExecuteJson> getCodec() {
        return CODEC;
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CommandExecuteJson context) {
        if (!(user instanceof Player player)) return;

        int heldTicks = getMaxUseTime(stack, user, context) - remainingUseTicks;
        if (heldTicks < context.minHold.getValue()) {
            resetAnimation(user); // released too early
            return;
        }

        if (!(world instanceof ServerLevel serverWorld)) return;

        CommandSourceStack source;
        if (context.asPlayer && player instanceof ServerPlayer serverPlayer) {
            source = serverPlayer.createCommandSourceStack();
        } else {
            source = serverWorld.getServer().createCommandSourceStack();
        }

        if (context.atPlayer) {
            source = source.withPosition(player.position()).withEntity(player);
        }

        for (String cmd : context.command) {
            serverWorld.getServer().getCommands().performPrefixedCommand(source, cmd);
        }

        // Apply execute pose if configured
        if (context.executePose != null && player instanceof ServerPlayer serverPlayer) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(serverPlayer);
            if (facet != null) {
                facet.set(context.executePose, serverPlayer, player.getUsedItemHand());
            }
        } else {
            resetAnimation(user); // no execute pose, so reset immediately
        }

        player.getCooldowns().addCooldown(stack.getItem(), (int) context.cooldown.getValue());
        player.swing(player.getUsedItemHand());
    }

    private void resetAnimation(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(player);
            if (facet != null) {
                facet.reset(player);
            }
        }
    }

    @Override
    public int getCooldown(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).cooldown.getValue();
    }

    @Override
    public CommandExecuteJson getDefaultContext() {
        return null;
    }

    @Override
    public CommandExecuteJson initialize(CommandExecuteJson json, ModuleInstance moduleInstance) {
        return json.initialize(moduleInstance);
    }

    @Override
    public int getMinHoldTime(ItemStack itemStack) {
        return (int) getSpecialContext(itemStack).minHold.getValue();
    }

    @Override
    public CommandExecuteJson merge(CommandExecuteJson left, CommandExecuteJson right, MergeType mergeType) {
        CommandExecuteJson merged = new CommandExecuteJson();
        merged.command = new ArrayList<>(left.command);
        if (MergeType.OVERWRITE.equals(mergeType)) {
            merged.command = right.command;
        } else {
            merged.command.addAll(right.command);
        }
        merged.asPlayer = mergeType.equals(MergeType.OVERWRITE) ? right.asPlayer : left.asPlayer;
        merged.atPlayer = mergeType.equals(MergeType.OVERWRITE) ? right.atPlayer : left.atPlayer;
        merged.minHold = left.minHold.merge(right.minHold, mergeType);
        merged.cooldown = left.cooldown.merge(right.cooldown, mergeType);
        merged.userAnim = right.userAnim != null ? right.userAnim : left.userAnim;
        merged.startPose = right.startPose != null ? right.startPose : left.startPose;
        merged.executePose = right.executePose != null ? right.executePose : left.executePose;
        return merged;
    }

    public static class CommandExecuteJson {
        public List<String> command = new ArrayList<>();

        @CodecBehavior.Optional
        public boolean asPlayer = false;

        @CodecBehavior.Optional
        public boolean atPlayer = true;

        @AutoCodec.Name("min_hold")
        @CodecBehavior.Optional
        public DoubleOperationResolvable minHold = new DoubleOperationResolvable(0);

        @CodecBehavior.Optional
        public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(20);

        @CodecBehavior.Optional
        public UseAnim userAnim = UseAnim.NONE;

        @CodecBehavior.Optional
        public String startPose;

        @CodecBehavior.Optional
        public String executePose;

        public CommandExecuteJson initialize(ModuleInstance moduleInstance) {
            CommandExecuteJson json = new CommandExecuteJson();
            json.command = this.command;
            json.asPlayer = this.asPlayer;
            json.atPlayer = this.atPlayer;
            json.minHold = this.minHold.initialize(moduleInstance);
            json.cooldown = this.cooldown.initialize(moduleInstance);
            json.userAnim = this.userAnim;
            json.startPose = this.startPose;
            json.executePose = this.executePose;
            return json;
        }
    }
}
