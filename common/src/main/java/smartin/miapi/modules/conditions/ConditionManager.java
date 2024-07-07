package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConditionManager {
    public static MiapiRegistry<ModuleCondition> oldConditionRegistry = MiapiRegistry.getInstance(ModuleCondition.class);
    public static Map<ResourceLocation, Codec<ModuleCondition>> CONDITION_REGISTRY = new ConcurrentHashMap<>();
    public static ContextManager<ModuleInstance> MODULE_CONDITION_CONTEXT = source -> ((ModuleInstance) source).copy();
    public static ContextManager<BlockPos> WORKBENCH_LOCATION_CONTEXT = BlockPos.class::cast;
    public static ContextManager<Player> PLAYER_CONTEXT = Player.class::cast;
    public static ContextManager<Map<ModuleProperty<?>, Object>> MODULE_PROPERTIES = source -> new HashMap<>((Map<ModuleProperty<?>, Object>) source);

    public static Codec<ModuleCondition> CONDITION_CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            Pair<String, T> result = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("type")).getOrThrow();
            return CONDITION_REGISTRY.get(
                    Miapi.id(result.getFirst())).decode(ops, result.getSecond());
        }

        /**
         * Conditions should never be encoded. this should be implemented at some point
         */
        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };

    public static void setup() {
        //no onReload is required at the moment
    }

    public static ModuleCondition get(JsonElement element) {
        return CONDITION_CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
    }

    public static ConditionContext fullContext(ModuleInstance moduleInstance, BlockPos pos, Player player, Map<ModuleProperty<?>, Object> properties) {
        ConditionContext context = new ConditionContext();
        context.setContext(MODULE_CONDITION_CONTEXT, moduleInstance);
        context.setContext(WORKBENCH_LOCATION_CONTEXT, pos);
        context.setContext(PLAYER_CONTEXT, player);
        context.setContext(MODULE_PROPERTIES,properties);
        return context;
    }

    public static ConditionContext moduleContext(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties) {
        ConditionContext context = new ConditionContext();
        context.setContext(MODULE_CONDITION_CONTEXT, moduleInstance);
        context.setContext(MODULE_PROPERTIES,properties);
        return context;
    }

    public static class ConditionContext {
        Map<ContextManager<?>, Object> context = new HashMap<>();
        public List<Component> failReasons = new ArrayList<>();

        public ConditionContext copy() {
            ConditionContext copy = new ConditionContext();
            context.forEach((contextHolder, contextInfo) -> {
                context.put(contextHolder, contextHolder.copy(contextInfo));
            });
            return copy;
        }

        public <T> void setContext(ContextManager<T> contextManger, T actualData) {
            context.put(contextManger, actualData);
        }

        public <T> Optional<T> getContext(ContextManager<T> contextManger) {
            return Optional.ofNullable((T) context.get(contextManger));
        }
    }

    public interface ContextManager<T> {
        T copy(Object source);
    }
}
