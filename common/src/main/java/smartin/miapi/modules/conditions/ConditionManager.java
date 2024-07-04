package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.MiapiRegistry;

import java.util.*;

public class ConditionManager {
    public static MiapiRegistry<ModuleCondition> moduleConditionRegistry = MiapiRegistry.getInstance(ModuleCondition.class);
    public static ContextManager<ModuleInstance> MODULE_CONDITION_CONTEXT = source -> ((ModuleInstance) source).copy();
    public static ContextManager<BlockPos> WORKBENCH_LOCATION_CONTEXT = BlockPos.class::cast;
    public static ContextManager<Player> PLAYER_LOCATION_CONTEXT = Player.class::cast;
    public static ContextManager<Map<ModuleProperty<?>, Object>> MODULE_PROPERTIES = source -> new HashMap<>((Map<ModuleProperty<?>, Object>)source);

    public static Codec<ModuleCondition> CONDITIONCODEC = new Codec<ModuleCondition>() {
        @Override
        public <T> DataResult<Pair<ModuleCondition, T>> decode(DynamicOps<T> ops, T input) {
            return null;
        }

        @Override
        public <T> DataResult<T> encode(ModuleCondition input, DynamicOps<T> ops, T prefix) {
            return null;
        }
    };

    public static void setup() {
        //no onReload is required at the moment
    }

    public static ModuleCondition get(JsonElement element) {
        if (element == null) {
            return new TrueCondition();
        }
        ModuleCondition condition = moduleConditionRegistry.get(element.getAsJsonObject().get("type").getAsString());
        assert condition != null;
        return condition.load(element);
    }

    public class ConditionContext {
        Map<ContextManager<?>, Object> context = new HashMap<>();
        List<Component> failReasons = new ArrayList<>();

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
