package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.modules.MiapiPermissions;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MiapiPerm implements ModuleCondition {

    List<String> perms = new ArrayList<>();

    public MiapiPerm() {

    }

    public MiapiPerm(List<String> perms) {
        this.perms = perms;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if (conditionContext instanceof ConditionManager.ModuleConditionContext moduleConditionContext) {
            Player player = moduleConditionContext.player;
            List<Component> reasons = moduleConditionContext.reasons;
            if (player != null && MiapiPermissions.hasPerm(player, perms)) {
                return true;
            }
            reasons.add(Component.literal("This is a Cosmetic for Kofi and Patreon supporter."));
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        List<String> permList = new ArrayList<>();
        element.getAsJsonObject().get("perms").getAsJsonArray().forEach(element1 -> {
            permList.add(element1.getAsString());
        });
        return new MiapiPerm(permList);
    }
}
