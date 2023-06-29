package smartin.miapi.modules.conditions;

import com.google.gson.JsonElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.MaterialProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;
import java.util.Map;

public class ModuleTypeCondition implements ModuleCondition {
    public ItemModule module;

    public ModuleTypeCondition() {

    }

    public ModuleTypeCondition(ItemModule module) {
        this.module = module;
    }

    @Override
    public boolean isAllowed(ItemModule.ModuleInstance moduleInstance, @Nullable PlayerEntity player, Map<ModuleProperty, JsonElement> propertyMap, List<Text> reasons) {
        if (moduleInstance != null && moduleInstance.module.equals(module)) {
            return true;
        }
        return false;
    }

    @Override
    public ModuleCondition load(JsonElement element) {
        return new ModuleTypeCondition(RegistryInventory.modules.get(element.getAsJsonObject().get("module").getAsString()));
    }
}
