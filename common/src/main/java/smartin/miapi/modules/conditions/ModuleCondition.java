package smartin.miapi.modules.conditions;

import net.minecraft.resources.ResourceLocation;

public interface ModuleCondition {

    boolean isAllowed(ConditionManager.ConditionContext conditionContext);

    ResourceLocation getID();
}
