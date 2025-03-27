package smartin.miapi.editor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.editor.syntax.JsonSyntaxHighlighter;
import smartin.miapi.editor.syntax.PropertyMapHighlighter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;

public class EditorCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> runPose = Commands.literal("miapi")
                .then(Commands.literal("editor")
                        .then(Commands.literal("hand")
                                .executes(EditorCommands::executeHandEditor)));
        LiteralArgumentBuilder<CommandSourceStack> fs = Commands.literal("miapi")
                .then(Commands.literal("editor")
                        .then(Commands.literal("data")
                                .executes(EditorCommands::executeOpenEditor)));
        dispatcher.register(runPose);
        dispatcher.register(fs);
        JsonEditor.registerGlobalInterface(new JsonSyntaxHighlighter());
        JsonEditor.registerGlobalInterface(new PropertyMapHighlighter());
        // Register default JSON syntax highlighter
        EditorEvents.EDITOR_INTERFACES.register(event -> {
            if (event.filePath.endsWith(".json")) {
                event.interfaces.add(new JsonSyntaxHighlighter());
            }
            return EventResult.pass();
        });

        // Register PropertyMapHighlighter for module files
        EditorEvents.EDITOR_INTERFACES.register(event -> {
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/modules/")) {
                event.interfaces.add(new PropertyMapHighlighter(event.resourceLocation));
            }
            return EventResult.pass();
        });
    }

    private static int executeHandEditor(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            if (Platform.getEnv().equals(EnvType.SERVER)) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            if (!Minecraft.getInstance().player.getUUID().equals(context.getSource().getPlayer().getUUID())) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            ItemStack itemStack = context.getSource().getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null) {
                ModuleEditor moduleEditor = new ModuleEditor(moduleInstance.copy(), (m) -> {
                    m.copy().writeToItem(itemStack);
                });
                MiapiEditor.editors.add(moduleEditor);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Hand Item is not a valid modular item!"));
                return -1;
            }
        } else {
            context.getSource().sendFailure(Component.literal("Only Player can execute this command!"));
            return -1;
        }
    }

    private static int executeOpenEditor(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            if (Platform.getEnv().equals(EnvType.SERVER)) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            if (!Minecraft.getInstance().player.getUUID().equals(context.getSource().getPlayer().getUUID())) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            MiapiEditor.editors.add(new LiveDataPackEditorManager());
            return 1; // Return success
        } else {
            context.getSource().sendFailure(Component.literal("Only Player can execute this command!"));
            return -1;
        }
    }
}
