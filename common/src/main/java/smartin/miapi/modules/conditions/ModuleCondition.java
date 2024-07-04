package smartin.miapi.modules.conditions;

public interface ModuleCondition {

    boolean isAllowed(ConditionManager.ConditionContext conditionContext);
}
