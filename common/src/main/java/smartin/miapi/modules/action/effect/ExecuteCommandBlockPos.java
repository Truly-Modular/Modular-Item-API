package smartin.miapi.modules.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.action.ActionContext;
import smartin.miapi.modules.action.ActionEffect;

import java.util.List;
import java.util.Optional;

public record ExecuteCommandBlockPos(String command, String key, ResourceLocation type) implements ActionEffect {
    public static final ResourceLocation TYPE = Miapi.id("execute_command_pos");
    public static final Codec<ExecuteCommandBlockPos> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("command").forGetter(ExecuteCommandBlockPos::command),
                    Codec.STRING.fieldOf("at").forGetter(ExecuteCommandBlockPos::key),
                    ResourceLocation.CODEC.fieldOf("type").orElse(TYPE).forGetter(ExecuteCommandBlockPos::type)
            ).apply(instance, ExecuteCommandBlockPos::new));

    @Override
    public List<String> dependency(ActionContext context) {
        return List.of(key);
    }

    @Override
    public boolean setup(ActionContext context) {
        return context.getObject(BlockPos.class, key).isPresent();
    }

    @Override
    public void execute(ActionContext context) {
        Optional<List<BlockPos>> posOpt = context.getList(BlockPos.class, key);
        posOpt.ifPresent(posList -> {
            // Execute the command at the specified BlockPos
            posList.forEach(pos -> {
                context.level.getServer().getCommands().performCommand(
                        null,
                        "execute positioned " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " +
                        command
                );
            });
        });
    }

    @Override
    public ExecuteCommandBlockPos initialize(ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }
}