package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;

public class IsModLoadedCondition implements ModuleCondition {
    public String mod = "";

    public IsModLoadedCondition() {

    }

    public IsModLoadedCondition(String material) {
        this.mod = material;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        if(Platform.isModLoaded(mod)) {
            return true;
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new IsModLoadedCondition(element.getAsJsonObject().get("mod").getAsString());
    }
}
