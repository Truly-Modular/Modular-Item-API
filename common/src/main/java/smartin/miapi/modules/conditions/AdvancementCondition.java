package smartin.miapi.modules.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
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
import java.util.Optional;

public class AdvancementCondition implements ModuleCondition {
    public static Codec<ModuleCondition> CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("advancement")).getOrThrow();
            return DataResult.success(new Pair(new AdvancementCondition(ResourceLocation.parse(result.getFirst())), result.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "encoding condition is not fully supported");
        }
    };

    public ResourceLocation advancement;

    public AdvancementCondition() {

    }

    public AdvancementCondition(ResourceLocation perms) {
        this.advancement = perms;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<Player> playerOptional = conditionContext.getContext(ConditionManager.PLAYER_CONTEXT);
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            List<Component> reasons = conditionContext.failReasons;
            if (advancement != null) {
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
