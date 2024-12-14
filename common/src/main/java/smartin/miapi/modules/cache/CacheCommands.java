package smartin.miapi.modules.cache;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.network.Networking;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command related to materials- used to fetch debug data of active materials
 */
public class CacheCommands {
    public static String SEND_MATERIAL_CLIENT = "miapi_drop_cache";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("miapi")
                .then(Commands.literal("clear_cache")
                        .executes(CacheCommands::executeCacheClear));

        LiteralArgumentBuilder<CommandSourceStack> reloadCommand = Commands.literal("miapi")
                .then(Commands.literal("miapi_reload")
                        .executes(CacheCommands::executeMiapiReload));

        dispatcher.register(literal);
        dispatcher.register(reloadCommand);
    }

    private static int executeCacheClear(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Clearing all miapi Caches"), false);
        if (context.getSource().isPlayer()) {
            FriendlyByteBuf buf = Networking.createBuffer();
            buf.writeBoolean(true);
            Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
        }
        ModularItemCache.discardCache();
        return 1; // Return success
    }

    private static int executeMiapiReload(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("starting reload"), false);
        return 1; // Return success
    }

    public static void triggerServerReload() {

        ReloadEvents.reloadCounter++;
        Map<ResourceLocation, String> cacheDatapack = new LinkedHashMap<>(ReloadEvents.DATA_PACKS);
        ReloadEvents.START.fireEvent(false, Miapi.server.registryAccess());
        ReloadEvents.DataPackLoader.trigger(cacheDatapack);
        ReloadEvents.MAIN.fireEvent(false, Miapi.server.registryAccess());
        ReloadEvents.END.fireEvent(false, Miapi.server.registryAccess());
        ReloadEvents.reloadCounter = 0;
        Miapi.server.getPlayerList().getPlayers().forEach(ReloadEvents::triggerReloadOnClient);
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
        return MaterialProperty.materials.keySet().stream().map(ResourceLocation::toString).toList();
    }
}
