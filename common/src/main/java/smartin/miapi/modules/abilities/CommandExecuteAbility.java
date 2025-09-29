package smartin.miapi.modules.abilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import smartin.miapi.modules.abilities.util.ItemAbilityManager;
import smartin.miapi.modules.abilities.util.ItemUseDefaultCooldownAbility;
import smartin.miapi.modules.abilities.util.ItemUseMinHoldAbility;
import smartin.miapi.modules.properties.AbilityMangerProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes configured commands when triggered.
 * Configurable fields (via contextJson):
 * - command: string or array of strings
 * - asPlayer: boolean (default false)
 * - atPlayer: boolean (default true)
 * - minHold: double (default 20)
 * - cooldown: double (default 20)
 * - userAnim: string (UseAction, default SPEAR)
 */
public class CommandExecuteAbility implements ItemUseDefaultCooldownAbility, ItemUseMinHoldAbility {
    public static final String KEY = "command";

    public CommandExecuteAbility() {
    }

    @Override
    public boolean allowedOnItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand, ItemAbilityManager.AbilityHitContext abilityHitContext) {
        AbilityMangerProperty.AbilityContext context = getAbilityContext(itemStack);
        JsonObject json = context.contextJson;
        return json.has(KEY);
    }

    @Override
    public UseAction getUseAction(ItemStack itemStack) {
        AbilityMangerProperty.AbilityContext context = getAbilityContext(itemStack);
        JsonObject json = context.contextJson;

        if (json.has("userAnim")) {
            try {
                String animName = json.get("userAnim").getAsString().toUpperCase();
                return UseAction.valueOf(animName);
            } catch (Exception ignored) {
                return UseAction.NONE;
            }
        }
        return UseAction.NONE;
    }

    @Override
    public int getMaxUseTime(ItemStack itemStack) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.getItemCooldownManager().isCoolingDown(user.getStackInHand(hand).getItem())) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsingAfter(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return;

        AbilityMangerProperty.AbilityContext context = getAbilityContext(stack);
        JsonObject json = context.contextJson;

        double minHold = json.has("minHold") ? json.get("minHold").getAsDouble() : 20;
        double cooldown = json.has("cooldown") ? json.get("cooldown").getAsDouble() : 20;
        boolean runAsPlayer = json.has("asPlayer") && json.get("asPlayer").getAsBoolean();
        boolean runAtPlayer = !json.has("atPlayer") || json.get("atPlayer").getAsBoolean();

        if (getMaxUseTime(stack) - remainingUseTicks < minHold) return;

        if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            if (json.has(KEY)) {
                List<String> commands = new ArrayList<>();
                JsonElement cmdElement = json.get(KEY);

                if (cmdElement.isJsonArray()) {
                    JsonArray array = cmdElement.getAsJsonArray();
                    for (JsonElement el : array) {
                        if (el.isJsonPrimitive()) {
                            commands.add(el.getAsString());
                        }
                    }
                } else if (cmdElement.isJsonPrimitive()) {
                    commands.add(cmdElement.getAsString());
                }

                for (String cmd : commands) {
                    try {
                        ServerCommandSource source;
                        if (runAsPlayer) {
                            source = serverPlayer.getCommandSource();
                        } else {
                            source = serverWorld.getServer().getCommandSource();
                        }

                        if (runAtPlayer) {
                            source = source.withPosition(player.getPos());
                        }

                        serverWorld.getServer().getCommandManager().executeWithPrefix(source, cmd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            player.swingHand(player.getActiveHand());
            player.getItemCooldownManager().set(stack.getItem(), (int) cooldown);
        }
    }
}
