package smartin.miapi.modules.action.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.action.ActionContext;
import smartin.miapi.modules.action.ActionEffect;

import java.util.List;
import java.util.Optional;

public record ExecuteCommandEntity(String command, String key, String commandType,
                                   ResourceLocation type) implements ActionEffect {
    public static final ResourceLocation TYPE = Miapi.id("execute_command_entity");
    public static final Codec<ExecuteCommandEntity> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.STRING.fieldOf("command").forGetter(ExecuteCommandEntity::command),
                    Codec.STRING.fieldOf("as").forGetter(ExecuteCommandEntity::key),
                    Codec.STRING.optionalFieldOf("command_type", "as").forGetter(ExecuteCommandEntity::commandType),
                    net.minecraft.resources.ResourceLocation.CODEC.fieldOf("type").orElse(TYPE).forGetter(ExecuteCommandEntity::type)
            ).apply(instance, ExecuteCommandEntity::new));

    @Override
    public List<String> dependency(ActionContext context) {
        return List.of(key);
    }

    @Override
    public boolean setup(ActionContext context) {
        return context.getList(Entity.class, key).isPresent();
    }

    @Override
    public void execute(ActionContext context) {
        Optional<List<Entity>> entityOpt = context.getList(Entity.class, key);
        entityOpt.ifPresent(entityList -> {
            entityList.forEach(entity -> {
                context.level.getServer().getCommands().performCommand(null,
                        "execute " + commandType + " " + entity.getUUID() + " " + command);
            });
        });
    }

    @Override
    public ExecuteCommandEntity initialize(ModuleInstance moduleInstance) {
        return this;
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }
}