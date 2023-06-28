package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import smartin.miapi.Miapi;
import smartin.miapi.registries.MiapiRegistry;

public class ConditionManager {
    public static MiapiRegistry<ModuleCondition> moduleConditionRegistry = MiapiRegistry.getInstance(ModuleCondition.class);

    public static void setup() {

    }

    public static ModuleCondition get(JsonElement element) {
        if (element == null) {
            return new TrueCondition();
        }
        ModuleCondition condition = moduleConditionRegistry.get(element.getAsJsonObject().get("type").getAsString());
        return condition.load(element);
    }
}
