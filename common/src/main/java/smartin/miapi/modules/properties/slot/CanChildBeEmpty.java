package smartin.miapi.modules.properties.slot;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class CanChildBeEmpty extends ComplexBooleanProperty {
    public static final ResourceLocation KEY = Miapi.id("can_child_be_empty");
    public static CanChildBeEmpty property;

    public CanChildBeEmpty() {
        super(KEY, true);
        property = this;
    }
}