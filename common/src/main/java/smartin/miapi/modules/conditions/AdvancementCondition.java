package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.client.ClientAdvancementManagerAccessor;

import java.util.List;

public class AdvancementCondition implements ModuleCondition {
    ResourceLocation advancement = null;

    public AdvancementCondition() {

    }

    public AdvancementCondition(ResourceLocation perms) {
        this.advancement = perms;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Player player = moduleConditionContext.player;
            List<Component> reasons = moduleConditionContext.reasons;
            if (player != null && advancement != null) {
                AdvancementHolder advancement1 = getAdvancement(advancement);
                if (advancement1 != null) {
                    return hasAdvancement(advancement1, player);
                }
            }
            reasons.add(Component.literal("Unavailable."));
        }
        return false;
    }

    public static boolean hasAdvancement(AdvancementHolder advancement, Player player) {
        if (smartin.miapi.Environment.isClient()) {
            return hasAdvancementClient(advancement.value(), player);
        }
        if (player instanceof ServerPlayer serverPlayerEntity) {
            return serverPlayerEntity.getAdvancements().getOrStartProgress(advancement).isDone();
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static boolean hasAdvancementClient(Advancement advancement, Player player) {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().getConnection() != null) {
            ClientAdvancements manager = Minecraft.getInstance().getConnection().getAdvancements();
            return ((ClientAdvancementManagerAccessor) manager).getProgress().get(advancement).isDone();
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            ResourceLocation identifier = ResourceLocation.parse(object.get("advancement").getAsString());
            return new AdvancementCondition(identifier);
        }
        return new NotCondition(new TrueCondition());
    }

    public AdvancementHolder getAdvancement(ResourceLocation identifier) {
        if (smartin.miapi.Environment.isClient()) {
            return getAdvancementClient(identifier);
        }
        if (Miapi.server != null) {
            return Miapi.server.getAdvancements().get(identifier);
        }
        return null;
    }

    public AdvancementHolder getAdvancementClient(ResourceLocation identifier) {
        if (Minecraft.getInstance().getConnection() != null) {
            return Minecraft.getInstance().getConnection().getAdvancements().getTree().get(identifier).holder();
        }
        return null;
    }
}
