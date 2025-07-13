package smartin.miapi.editor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.blueprint.BlueprintComponent;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.editor.material.MaterialEditor;
import smartin.miapi.editor.syntax.CodecValidatorInterface;
import smartin.miapi.editor.syntax.JsonSyntaxHighlighter;
import smartin.miapi.editor.syntax.PropertyMapHighlighter;
import smartin.miapi.material.CodecMaterial;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.abilities.key.MiapiBinding;
import smartin.miapi.modules.synergies.SynergyManager;

import java.util.function.Function;

import static smartin.miapi.editor.MiapiEditor.editors;

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
        LiteralArgumentBuilder<CommandSourceStack> materialEditor = Commands.literal("miapi")
                .then(Commands.literal("editor")
                        .then(Commands.literal("material")
                                .executes(EditorCommands::executeOpenMaterialEditor)));
        dispatcher.register(runPose);
        dispatcher.register(fs);
        dispatcher.register(materialEditor);
        if (Platform.getEnv() == EnvType.CLIENT) {
            registerClient();
        }
    }

    @Environment(EnvType.CLIENT)
    private static void registerClient() {
        JsonEditor.registerGlobalInterface(new JsonSyntaxHighlighter());
        JsonEditor.registerGlobalInterface(new PropertyMapHighlighter());
        var synergyValidator = new CodecValidatorInterface(SynergyManager.SYNERGY_CODEC, "Synergy Validator");
        JsonEditor.registerGlobalInterface(synergyValidator);
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
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/synergies/")) {
                event.interfaces.add(new CodecValidatorInterface(SynergyManager.SYNERGY_CODEC, "Synergy Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/modular_converters/")) {
                event.interfaces.add(new CodecValidatorInterface(ModuleInstance.CODEC, "Modular Converter Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/material/")) {
                event.interfaces.add(new CodecValidatorInterface(CodecMaterial.CODEC, "Material Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/blueprints/")) {
                event.interfaces.add(new CodecValidatorInterface(BlueprintComponent.CODEC, "Blueprint Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/key_binding")) {
                event.interfaces.add(new CodecValidatorInterface(MiapiBinding.CODEC, "KeyBind Validator"));
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/create_options/")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/material_extension/")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/module_extension/")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/skin/module")) {
                //TODO:validator
            }
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/skin/tab")) {
                //TODO:validator
            }
            return EventResult.pass();
        });
        if(Platform.isModLoaded("veil")){
            VeilEditor.setup();
        } else if (Platform.isModLoaded("nucleus_editor")) {
            NucleusEditor.setup();
        } else if (MiapiConfig.getClientConfig().other.allowEditorNoNucleus) {

        }
    }

    private static int executeHandEditor(CommandContext<CommandSourceStack> context) {
        return canExecute(context, (c) -> {
            ItemStack itemStack = context.getSource().getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
            ModuleInstance moduleInstance = ItemModule.getModules(itemStack);
            if (moduleInstance != null) {
                ModuleEditor moduleEditor = new ModuleEditor(moduleInstance.copy(), (m) -> {
                    m.copy().writeToItem(itemStack);
                });
                editors.add(moduleEditor);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("Hand Item is not a valid modular item!"));
                return -1;
            }
        });
    }

    private static int executeOpenEditor(CommandContext<CommandSourceStack> context) {
        return canExecute(context, (c) -> {
            editors.add(new LiveDataPackEditorManager());
            return 1; // Return success
        });
    }

    private static int executeOpenMaterialEditor(CommandContext<CommandSourceStack> context) {
        if (true) {
            context.getSource().sendFailure(Component.literal("Material editor is not yet finished!"));
            return 1;
        }
        return canExecute(context, (c) -> {
            editors.add(new MaterialEditor((CodecMaterial) MaterialProperty.MATERIAL_REGISTRY.get(Miapi.id("metal/iron")), (m) -> {

            }));
            return 1; // Return success
        });
    }

    public static int canExecute(CommandContext<CommandSourceStack> context, Function<CommandContext<CommandSourceStack>, Integer> onExecute) {
        if (context.getSource().isPlayer()) {
            if (Platform.getEnv().equals(EnvType.SERVER)) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            if (!Minecraft.getInstance().player.getUUID().equals(context.getSource().getPlayer().getUUID())) {
                context.getSource().sendFailure(Component.literal("Command only allowed in SinglePlayer"));
                return -1;
            }
            if (!context.getSource().hasPermission(4)) {
                context.getSource().sendFailure(Component.literal("Command only allowed for operators"));
                return -1;
            }
            if (!MiapiConfig.getClientConfig().other.allowEditorNoNucleus && !Platform.isModLoaded("nucleus_editor")) {
                context.getSource().sendFailure(Component.literal("Requires Nucleus Editor to be installed!"));
                return -1;
            }
            return onExecute.apply(context);
        } else {
            context.getSource().sendFailure(Component.literal("Only Player can execute this command!"));
            return -1;
        }
    }
}
