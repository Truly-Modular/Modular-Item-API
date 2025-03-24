package smartin.miapi.editor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
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
    }

    private static int executeHandEditor(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ItemStack itemStack = context.getSource().getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null) {
                ModuleEditor moduleEditor = new ModuleEditor(moduleInstance.copy(), (m) -> {
                    m.copy().writeToItem(itemStack);
                });
                MiapiEditor.editors.add(moduleEditor);
            }
        }
        return 1; // Return success
    }

    private static int executeOpenEditor(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            MiapiEditor.editors.add(new LiveDataPackEditorManager());
        }
        return 1; // Return success
    }
}
