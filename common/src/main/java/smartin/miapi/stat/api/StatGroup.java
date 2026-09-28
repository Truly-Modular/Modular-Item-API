package smartin.miapi.stat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.registries.MiapiRegistry;

public class StatGroup {
    public static final MiapiRegistry<StatGroup> STAT_GROUP_REGISTRY = MiapiRegistry.getInstance(StatGroup.class);


    public static StatGroup getOrRegister(ResourceLocation id) {
        return getOrRegister(id, 0);
    }

    public static StatGroup getOrRegister(ResourceLocation id, double priority) {
        StatGroup group = STAT_GROUP_REGISTRY.get(id);
        if (group != null) {
            return group;
        }
        return STAT_GROUP_REGISTRY.register(id, new StatGroup(id, 0));
    }

    ResourceLocation id;
    Component name;
    Component description;
    double priority;

    protected StatGroup(ResourceLocation id, double priority) {
        this.id = id;
        name = Component.translatable(Miapi.MOD_ID + ".stat.group" + id.getPath() + "." + id.getNamespace().replace("/", "."));
        description = Component.translatable(Miapi.MOD_ID + ".stat.group" + id.getPath() + "." + id.getNamespace().replace("/", ".") + ".description");
        this.priority = priority;
    }

    public ResourceLocation getID() {
        return id;
    }

    public Component getName() {
        return name;
    }

    public Component getDescription() {
        return description;
    }

    public double getPriority() {
        return priority;
    }
}