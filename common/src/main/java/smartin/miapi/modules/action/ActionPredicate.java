package smartin.miapi.modules.action;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

public interface ActionPredicate {

    List<String> dependency(ActionContext context);

    boolean setup(ActionContext context);

    ActionPredicate initialize(ModuleInstance moduleInstance);

    ResourceLocation getType();
}
