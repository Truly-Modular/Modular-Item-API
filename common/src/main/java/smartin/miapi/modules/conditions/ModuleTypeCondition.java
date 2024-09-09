package smartin.miapi.modules.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.Optional;
/**
 * @header Module
 * @description_start
 * this condition if the associated module instance uses this module
 * @desciption_end
 * @path /data_types/condition/module
 * @data type:module
 * @data module:the id of the module
 */
public class ModuleTypeCondition implements ModuleCondition {
    public static Codec<ModuleTypeCondition> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    ResourceLocation.CODEC
                            .fieldOf("module")
                            .forGetter((condition) -> condition.module.id()),
                    ComponentSerialization.CODEC
                            .optionalFieldOf("error", Component.translatable(Miapi.MOD_ID + ".condition.material.error"))
                            .forGetter((condition) -> condition.error)
            ).apply(instance, ModuleTypeCondition::new));
    @Nullable
    public ItemModule module;
    public Component error;

    public ModuleTypeCondition(ResourceLocation moduleID, Component error) {
        this.module = RegistryInventory.modules.get(moduleID);
        this.error = error;
    }

    @Override
    public boolean isAllowed(ConditionManager.ConditionContext conditionContext) {
        Optional<ModuleInstance> optional = conditionContext.getContext(ConditionManager.MODULE_CONDITION_CONTEXT);
        if (optional.isPresent() && module != null) {
            ModuleInstance moduleInstance = optional.get();
            return moduleInstance.module.equals(module);
        }
        return false;
    }
}
