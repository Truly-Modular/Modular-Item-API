package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.ClientAdvancementManagerAccessor;

import java.util.List;

public class AdvancementCondition implements ModuleCondition {
    Identifier advancement = null;

    public AdvancementCondition() {

    }

    public AdvancementCondition(Identifier perms) {
        this.advancement = perms;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            PlayerEntity player = moduleConditionContext.player;
            List<Text> reasons = moduleConditionContext.reasons;
            if (player != null && advancement != null) {
                Advancement advancement1 = getAdvancement(advancement);
                if(advancement1!=null){
                    return hasAdvancement(advancement1, player);
                }
            }
            reasons.add(Text.literal("Unavailable."));
        }
        return false;
    }

    public static boolean hasAdvancement(Advancement advancement, PlayerEntity player) {
        if (smartin.miapi.Environment.isClient()) {
            return hasAdvancementClient(advancement, player);
        }
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            return serverPlayerEntity.getAdvancementTracker().getProgress(advancement).isDone();
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static boolean hasAdvancementClient(Advancement advancement, PlayerEntity player) {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getNetworkHandler() != null) {
            ClientAdvancementManager manager = MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler();
            return ((ClientAdvancementManagerAccessor) manager).getAdvancementProgresses().get(advancement).isDone();
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            Identifier identifier = new Identifier(object.get("advancement").getAsString());
            return new AdvancementCondition(identifier);
        }
        return new NotCondition(new TrueCondition());
    }

    public Advancement getAdvancement(Identifier identifier) {
        if (smartin.miapi.Environment.isClient()) {
            return getAdvancementClient(identifier);
        }
        if (Miapi.server != null) {
            return Miapi.server.getAdvancementLoader().get(identifier);
        }
        return null;
    }

    public Advancement getAdvancementClient(Identifier identifier) {
        if(MinecraftClient.getInstance().getNetworkHandler()!=null){
            return MinecraftClient.getInstance().getNetworkHandler().getAdvancementHandler().getManager().get(identifier);
        }
        return null;
    }
}
