package smartin.miapi.modules.action;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ModuleInstance;

import java.util.List;

public interface ActionEffect extends ActionPredicate {

    List<String> dependency(ActionContext context);

    boolean setup(ActionContext context);

    void execute(ActionContext context);

    ActionEffect initialize(ModuleInstance moduleInstance);

    ResourceLocation getType();
}
