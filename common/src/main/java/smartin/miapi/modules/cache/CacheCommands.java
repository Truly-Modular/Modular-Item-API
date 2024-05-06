package smartin.miapi.modules.cache;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.network.Networking;

import java.util.Collection;
import java.util.List;

/**
 * A command related to materials- used to fetch debug data of active materials
 */
public class CacheCommands {
    public static String SEND_MATERIAL_CLIENT = "miapi_drop_cache";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal("miapi")
                .then(CommandManager.literal("clear_cache")
                        .executes(CacheCommands::executeCacheClear));

        LiteralArgumentBuilder<ServerCommandSource> reloadCommand = CommandManager.literal("miapi")
                .then(CommandManager.literal("miapi_reload")
                        .executes(CacheCommands::executeMiapiReload));

        dispatcher.register(literal);
        dispatcher.register(reloadCommand);
    }

    private static int executeCacheClear(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("Clearing all miapi Caches"), false);
        if (context.getSource().isExecutedByPlayer()) {
            PacketByteBuf buf = Networking.createBuffer();
            buf.writeBoolean(true);
            Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
        }
        ModularItemCache.discardCache();
        return 1; // Return success
    }

    private static int executeMiapiReload(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(() -> Text.literal("starting reload"), false);
        ReloadEvents.reloadCounter++;
        ReloadEvents.START.fireEvent(true);
        ReloadEvents.MAIN.fireEvent(true);
        ReloadEvents.END.fireEvent(true);
        ReloadEvents.reloadCounter--;
        Miapi.server.getPlayerManager().getPlayerList().forEach(ReloadEvents::triggerReloadOnClient);
        return 1; // Return success
    }

    static ArgumentType<String> getArgumentType() {
        return new ArgumentType<>() {
            @Override
            public String parse(StringReader reader) {
                return reader.getRead();
            }

            public Collection<String> getExamples() {
                return getMaterialOptions();
            }
        };
    }

    private static List<String> getMaterialOptions() {
        return MaterialProperty.materials.keySet().stream().toList();
    }
}
