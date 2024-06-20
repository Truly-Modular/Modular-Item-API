package smartin.miapi.registries;

import smartin.miapi.modules.properties.render.*;

import static smartin.miapi.registries.RegistryInventory.moduleProperties;
import static smartin.miapi.registries.RegistryInventory.registerMiapi;

public class ClientRegistryInventory {
    public static void registerClient(){
        registerMiapi(moduleProperties, ModelProperty.KEY, new ModelProperty());
        registerMiapi(moduleProperties, ModelTransformationProperty.KEY, new ModelTransformationProperty());
        registerMiapi(moduleProperties, ModelMergeProperty.KEY, new ModelMergeProperty());
        registerMiapi(moduleProperties, GuiOffsetProperty.KEY, new GuiOffsetProperty());
        registerMiapi(moduleProperties, ItemModelProperty.KEY, new ItemModelProperty());
    }
}
