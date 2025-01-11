package smartin.miapi.modules.abilities.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.redpxnda.nucleus.pose.client.PoseAnimationResourceListener;
import com.redpxnda.nucleus.pose.server.ServerPoseFacet;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Hand;

import java.util.Set;

/**
 * A command related to testing poses
 */
public class PoseCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> runPose = CommandManager.literal("miapi")
                .then(CommandManager.literal("pose")
                        .then(CommandManager.literal("run")
                                .then(CommandManager.argument("pose_id", IdentifierArgumentType.identifier())
                                        .suggests(POSE_SUGGESTIONS)
                                        .then(CommandManager.argument("off_hand", BoolArgumentType.bool())
                                                .executes(PoseCommands::executePose)))));

        LiteralArgumentBuilder<ServerCommandSource> stopPose = CommandManager.literal("miapi")
                .then(CommandManager.literal("pose")
                        .then(CommandManager.literal("stop")
                                .executes(PoseCommands::executePoseStop)));

        dispatcher.register(runPose);
        dispatcher.register(stopPose);
    }

    private static final SuggestionProvider<ServerCommandSource> POSE_SUGGESTIONS = (context, builder) -> {
        Set<String> materialOptions = PoseAnimationResourceListener.animations.keySet();
        materialOptions.forEach(builder::suggest);
        return builder.buildFuture();
    };

    private static int executePose(CommandContext<ServerCommandSource> context) {
        String poseId = IdentifierArgumentType.getIdentifier(context, "pose_id").toString();
        boolean offHand = BoolArgumentType.getBool(context, "off_hand");
        if (context.getSource().isExecutedByPlayer()) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(context.getSource().getPlayer());
            if (facet != null) {
                facet.set(poseId, context.getSource().getPlayer(), offHand ? Hand.MAIN_HAND : Hand.OFF_HAND);
            }
        }
        return 1; // Return success
    }

    private static int executePoseStop(CommandContext<ServerCommandSource> context) {
        if (context.getSource().isExecutedByPlayer()) {
            ServerPoseFacet facet = ServerPoseFacet.KEY.get(context.getSource().getPlayer());
            facet.reset(context.getSource().getPlayer());
        }
        return 1; // Return success
    }
}
