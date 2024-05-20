package smartin.miapi.modules.material;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import smartin.miapi.network.Networking;

import java.util.Collection;
import java.util.List;

/**
 * A command related to materials- used to fetch debug data of active materials
 */
public class MaterialCommand {
    public static String SEND_MATERIAL_CLIENT = "miapi_material_debug";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literal = CommandManager.literal("miapi")
                .then(CommandManager.literal("material")
                        .then(CommandManager.argument("material_id", StringArgumentType.word())
                                .suggests(MATERIAL_SUGGESTIONS) // Specify suggestion provider
                                .executes(MaterialCommand::executeMaterialCommand)));
        LiteralArgumentBuilder<ServerCommandSource> getHand = CommandManager.literal("miapi")
                .then(CommandManager.literal("get-hand-material")
                        .executes(MaterialCommand::executeMaterialCommand));

        dispatcher.register(getHand);
    }

    private static int getHandMaterial(CommandContext<ServerCommandSource> context) {
        if (context.getSource().isExecutedByPlayer()) {
            PlayerEntity player = context.getSource().getPlayer();
            Material material = MaterialProperty.getMaterialFromIngredient(player.getMainHandStack());
            if(material!=null){
                player.sendMessage(Text.literal("Handheld Material " + material.getKey()));
                PacketByteBuf buf = Networking.createBuffer();
                buf.writeString(material.getKey());
                Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
            }else{
                player.sendMessage(Text.literal("Handheld Material " + material.getKey()));
            }
            return 1; // Return success
        } else {
            // Material ID is not valid
            context.getSource().sendError(Text.literal("Handheld Item is no Material"));
            return 0; // Return failure
        }
    }

    private static int executeMaterialCommand(CommandContext<ServerCommandSource> context) {
        String materialId = StringArgumentType.getString(context, "material_id");
        List<String> materialOptions = getMaterialOptions(); // You need to define this method to getVertexConsumer the list of material options
        if (materialOptions.contains(materialId)) {
            // Material ID is valid, perform desired action
            context.getSource().sendFeedback(() -> Text.literal("Material ID is valid: " + materialId), false);
            if (context.getSource().isExecutedByPlayer()) {
                PacketByteBuf buf = Networking.createBuffer();
                buf.writeString(materialId);
                Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
            }
            return 1; // Return success
        } else {
            // Material ID is not valid
            context.getSource().sendError(Text.literal("Invalid material ID: " + materialId));
            return 0; // Return failure
        }
    }

    // Suggestion provider for material options
    private static final SuggestionProvider<ServerCommandSource> MATERIAL_SUGGESTIONS = (context, builder) -> {
        List<String> materialOptions = getMaterialOptions();
        materialOptions.forEach(builder::suggest);
        return builder.buildFuture();
    };

    // Method to suggest material options
    private static List<Suggestion> suggestMaterialOptions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        getMaterialOptions().forEach(builder::suggest);
        return builder.buildFuture().join().getList();
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
