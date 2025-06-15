package smartin.miapi.modules.properties.mining;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ComplexBooleanProperty;

public class MiningTelekinesisProperty extends ComplexBooleanProperty {
    public static MiningTelekinesisProperty property;
    public static ResourceLocation KEY = Miapi.id("mining_telekinesis");

    public MiningTelekinesisProperty() {
        super(KEY, false);
        property = this;

    }
}
