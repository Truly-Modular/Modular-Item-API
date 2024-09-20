package smartin.miapi.material;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import smartin.miapi.network.Networking;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * A command related to materials- used to fetch debug data of active materials
 */
public class MaterialCommand {
    public static String SEND_MATERIAL_CLIENT = "miapi_material_debug";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("miapi")
                .then(Commands.literal("material")
                        .then(Commands.argument("material_id", StringArgumentType.word())
                                .suggests(MATERIAL_SUGGESTIONS) // Specify suggestion provider
                                .executes(MaterialCommand::executeMaterialCommand)));
        LiteralArgumentBuilder<CommandSourceStack> getHand = Commands.literal("miapi")
                .then(Commands.literal("get-hand-material")
                        .executes(MaterialCommand::getHandMaterial));
        dispatcher.register(literal);
        dispatcher.register(getHand);
    }

    private static int getHandMaterial(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Player player = context.getSource().getPlayer();
            Material material = MaterialProperty.getMaterialFromIngredient(player.getMainHandItem());
            if (material != null) {
                player.sendSystemMessage(Component.literal("Handheld Material " + material.getID()));
                FriendlyByteBuf buf = Networking.createBuffer();
                buf.writeUtf(material.getID().toString());
                Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
            } else {
                player.sendSystemMessage(Component.literal("Handheld Item is no Material"));
            }
            return 1; // Return success
        } else {
            // Material ID is not valid
            context.getSource().sendFailure(Component.literal("Handheld Item is no Material"));
            return 0; // Return failure
        }
    }

    private static int executeMaterialCommand(CommandContext<CommandSourceStack> context) {
        String materialId = StringArgumentType.getString(context, "material_id");
        List<String> materialOptions = getMaterialOptions(); // You need to define this method to getVertexConsumer the list of material options
        if (materialOptions.contains(materialId)) {
            // Material ID is valid, perform desired action
            context.getSource().sendSuccess(() -> Component.literal("Material ID is valid: " + materialId), false);
            if (context.getSource().isPlayer()) {
                FriendlyByteBuf buf = Networking.createBuffer();
                buf.writeUtf(materialId);
                Networking.sendS2C(SEND_MATERIAL_CLIENT, context.getSource().getPlayer(), buf);
            }
            return 1; // Return success
        } else {
            // Material ID is not valid
            context.getSource().sendFailure(Component.literal("Invalid material ID: " + materialId));
            return 0; // Return failure
        }
    }

    // Suggestion provider for material options
    private static final SuggestionProvider<CommandSourceStack> MATERIAL_SUGGESTIONS = (context, builder) -> {
        List<String> materialOptions = getMaterialOptions();
        materialOptions.forEach(builder::suggest);
        return builder.buildFuture();
    };

    private static List<String> getMaterialOptions() {
        return MaterialProperty.materials.values().stream().map(Material::getStringID).toList();
    }
}
