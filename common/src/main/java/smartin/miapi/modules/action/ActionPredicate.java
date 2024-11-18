package smartin.miapi.modules.action;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

public interface ActionPredicate {

    default boolean dependencyMet(ActionContext context){
        return dependency(context).stream()
                .allMatch(key -> context.getList(Object.class, key).isPresent() ||
                                 context.getObject(Object.class, key).isPresent());
    }

    List<String> dependency(ActionContext context);

    boolean setup(ActionContext context);

    ActionPredicate initialize(ModuleInstance moduleInstance);

    ResourceLocation getType();
}
