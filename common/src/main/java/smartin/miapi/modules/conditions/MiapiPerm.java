package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.modules.MiapiPermissions;

import java.util.List;
import java.util.Optional;
/**
 * @header Miapi Perm Condition
 * @description_start
 * this condition is to check patreon or other types of outside conditions like time of year.
 * This is mostly used to give patreon subscribers their skins/enable free skins depending on the season
 * @desciption_end
 * @path /data_types/condition/miapi_perm
 * @data type:miapi_perm
 * @data perm:a list of perms or UUIDS
 */
public class MiapiPerm implements ModuleCondition {
    public static Codec<MiapiPerm> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.list(Codec.STRING).fieldOf("perms")
                            .forGetter((condition) -> condition.perms),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.literal("This is a Cosmetic for Kofi and Patreon supporter."))
                            .forGetter((condition) -> condition.onFail)
            ).apply(instance, MiapiPerm::new));

    List<String> perms;
    Component onFail;

    public MiapiPerm(List<String> perms, Component onFail) {
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
