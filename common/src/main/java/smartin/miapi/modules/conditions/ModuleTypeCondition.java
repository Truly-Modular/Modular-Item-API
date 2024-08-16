package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.Optional;

public class ModuleTypeCondition implements ModuleCondition {
    public static Codec<ModuleTypeCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING
                            .fieldOf("module")
                            .forGetter((condition) -> condition.module.id().toString()),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".condition.material.error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, ModuleTypeCondition::new));

    public ItemModule module;
    public Component error;

    public ModuleTypeCondition(String moduleID, Component error) {
        this.module = RegistryInventory.modules.get(moduleID);
        this.error = error;
    }

    public ModuleTypeCondition(ItemModule module) {
        this.module = module;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent()) {
            ModuleInstance moduleInstance = optional.get();
            return moduleInstance.module.equals(module);
        }
        return false;
    }
}
