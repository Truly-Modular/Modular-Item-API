package smartin.miapi.editor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.redpxnda.nucleus.editor.core.ClientLoader;
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
        dispatcher.register(runPose);

    }

    private static int executeHandEditor(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ItemStack itemStack = context.getSource().getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null) {
                ModuleEditor moduleEditor = new ModuleEditor(moduleInstance.copy(), (m) -> {
                    m.copy().writeToItem(itemStack);
                });
                ClientLoader.RENDER.add(moduleEditor);
            }
        }
        return 1; // Return success
    }
}
