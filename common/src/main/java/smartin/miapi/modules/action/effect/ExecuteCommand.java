package smartin.miapi.modules.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.action.ActionContext;
import smartin.miapi.modules.action.ActionEffect;

import java.util.List;

public record ExecuteCommand(String command,
                             ResourceLocation type) implements ActionEffect {
    public static final ResourceLocation TYPE = Miapi.id("execute_command");
    public static final Codec<ExecuteCommand> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("command").forGetter(ExecuteCommand::command),

                    ResourceLocation.CODEC.fieldOf("type").orElse(TYPE).forGetter(ExecuteCommand::type)
            ).apply(instance, ExecuteCommand::new));

    @Override
    public List<String> dependency(ActionContext context) {
        return List.of();
    }

    @Override
    public boolean setup(ActionContext context) {
        return true;
    }

    @Override
    public void execute(ActionContext context) {
        context.level.getServer().getCommands().performCommand(null,
                command);
    }

    @Override
    public ExecuteCommand initialize(ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }
}