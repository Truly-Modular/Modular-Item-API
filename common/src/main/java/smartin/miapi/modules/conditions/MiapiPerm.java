package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.modules.MiapiPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MiapiPerm implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<List<String>, T> result =  Codec.list(Codec.STRING).decode(ops, ops.getMap(input).getOrThrow().get("conditions")).getOrThrow();
            Component warning = ComponentSerialization.CODEC
                    .parse(ops, ops.getMap(input)
                            .getOrThrow()
                            .get("error"))
                    .result().orElse(Component.literal("This is a Cosmetic for Kofi and Patreon supporter."));
            return DataResult.success(new Pair(new MiapiPerm(result.getFirst(), warning), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };

    List<String> perms = new ArrayList<>();
    Component onFail = null;

    public MiapiPerm() {

    }

    public MiapiPerm(List<String> perms,Component onFail) {
        this.perms = perms;
        this.onFail = onFail;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Player> playerOptional = conditionContext.getContext(ConditionManager.PLAYER_CONTEXT);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            List<Component> reasons = conditionContext.failReasons;
            if (MiapiPermissions.hasPerm(player, perms)) {
                return true;
            }
            reasons.add(Component.literal("This is a Cosmetic for Kofi and Patreon supporter."));
        }
        return false;
    }
}
