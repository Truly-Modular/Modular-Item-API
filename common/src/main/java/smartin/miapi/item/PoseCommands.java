package smartin.miapi.item;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.redpxnda.nucleus.pose.client.PoseAnimationResourceListener;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.world.InteractionHand;

import java.util.Set;

/**
 * A command related to testing poses
 */
public class PoseCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> runPose = Commands.literal("miapi")
                .then(Commands.literal("pose")
                        .then(Commands.literal("run")
                                .then(Commands.argument("pose_id", ResourceLocationArgument.id())
                                        .suggests(POSE_SUGGESTIONS)
                                        .then(Commands.argument("off_hand", BoolArgumentType.bool())
                                                .executes(PoseCommands::executePose)))));

        LiteralArgumentBuilder < CommandSourceStack > stopPose = Commands.literal("miapi")
                .then(Commands.literal("pose")
                        .then(Commands.literal("stop")
                                .executes(PoseCommands::executePoseStop)));

        dispatcher.register(runPose);
        dispatcher.register(stopPose);
    }

    private static final SuggestionProvider<CommandSourceStack> POSE_SUGGESTIONS = (context, builder) -> {
        Set<String> materialOptions = PoseAnimationResourceListener.animations.keySet();
        materialOptions.forEach(builder::suggest);
        return builder.buildFuture();
    };

    private static int executePose(CommandContext<CommandSourceStack> context) {
        String poseId = ResourceLocationArgument.getId(context, "pose_id").toString();
        boolean offHand = BoolArgumentType.getBool(context, "off_hand");
        if (context.getSource().isPlayer()) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(context.getSource().getPlayer());
            if (facet != null) {
                facet.set(poseId, context.getSource().getPlayer(), offHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            }
        }
        return 1; // Return success
    }

    private static int executePoseStop(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(context.getSource().getPlayer());
            facet.reset(context.getSource().getPlayer());
        }
        return 1; // Return success
    }
}
