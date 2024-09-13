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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @header Conditions
 * @description_start
 * Conditions! what is there to describe
 * @desciption_end
 * @keywords Condition
 * @path /data_types/condition
 */
public class ConditionManager {
    public static Map<ResourceLocation, Codec<? extends ModuleCondition>> CONDITION_REGISTRY = new ConcurrentHashMap<>();
    public static ContextManager<ModuleInstance> MODULE_CONDITION_CONTEXT = source -> ((ModuleInstance) source);
    public static ContextManager<BlockPos> WORKBENCH_LOCATION_CONTEXT = BlockPos.class::cast;
    public static ContextManager<Player> PLAYER_CONTEXT = Player.class::cast;
    public static ContextManager<Map<ModuleProperty<?>, Object>> MODULE_PROPERTIES = source -> new HashMap<>((Map<ModuleProperty<?>, Object>) source);

    public static Codec<? extends ModuleCondition> CONDITION_CODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            var idRestult = Codec.STRING.decode(ops, ops.getMap(input).getOrThrow().get("type"));
            if (idRestult.isError()) {
                return DataResult.error(() -> "failed to decode condition - type was missing");
            }
            Pair<String, T> id = idRestult.getOrThrow();
            Codec<? extends ModuleCondition> conditionCodec = CONDITION_REGISTRY.get(Miapi.id(id.getFirst()));
            if(conditionCodec==null){
                return DataResult.error(() -> "failed to decode condition - type is not a condition:" + Miapi.id(id.getFirst()));
            }
            var result = conditionCodec.decode(ops, input);
            if (result.isSuccess()) {
                return DataResult.success(new Pair<>(result.getOrThrow().getFirst(), result.getOrThrow().getSecond()));
            }
            return DataResult.error(() -> "failed to decode condition " + result.error().get().message());
        }

        /**
         * Conditions should never be encoded. this should be implemented at some point
         */
        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.error(() -> "conditions cannot be encoded. This feature might be added later");
        }
    };

    public static Codec<ModuleCondition> CONDITION_CODEC_DIRECT = new Codec<ModuleCondition>() {

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return CONDITION_CODEC.encode(cast(input), ops, prefix);
        }


        @SuppressWarnings("unchecked")
        public <T extends ModuleCondition> T cast(ModuleCondition condition) {
            return (T) condition;
        }

        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<? extends Pair<? extends ModuleCondition, T>> firstResult = CONDITION_CODEC.decode(ops, input);
            if (firstResult.isError()) {
                return DataResult.error(() -> firstResult.error().get().message());
            }
            var pair = firstResult.getOrThrow();
            return DataResult.success(new Pair<>((ModuleCondition) pair.getFirst(), pair.getSecond()));
        }
    };

    public static void setup() {
        //no onReload is required at the moment
    }

    public static ModuleCondition get(JsonElement element) {
        try {
            return CONDITION_CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("issue during condition decoding " + e);
            Miapi.LOGGER.error("" + element);
            e.fillInStackTrace();
            return new TrueCondition();
        }
    }

    public static ConditionContext fullContext(ModuleInstance moduleInstance, BlockPos pos, Player player, Map<ModuleProperty<?>, Object> properties) {
        ConditionContext context = new ConditionContext();
        context.setContext(MODULE_CONDITION_CONTEXT, moduleInstance);
        context.setContext(WORKBENCH_LOCATION_CONTEXT, pos);
        context.setContext(PLAYER_CONTEXT, player);
        context.setContext(MODULE_PROPERTIES, properties);
        return context;
    }

    public static ConditionContext playerContext(ModuleInstance moduleInstance, Player player, Map<ModuleProperty<?>, Object> properties) {
        ConditionContext context = new ConditionContext();
        context.setContext(MODULE_CONDITION_CONTEXT, moduleInstance);
        context.setContext(PLAYER_CONTEXT, player);
        context.setContext(MODULE_PROPERTIES, properties);
        return context;
    }

    public static ConditionContext moduleContext(ModuleInstance moduleInstance, Map<ModuleProperty<?>, Object> properties) {
        ConditionContext context = new ConditionContext();
        context.setContext(MODULE_CONDITION_CONTEXT, moduleInstance);
        context.setContext(MODULE_PROPERTIES, properties);
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
