package smartin.miapi.modules.properties.compat.ht_treechop;


import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class TreechopProperty extends DoubleProperty {
    public static ResourceLocation KEY = Miapi.id("ht_treechop_swing");
    public static TreechopProperty property;

    public TreechopProperty() {
        super(KEY);
        property = this;
    }

    public boolean load(ResourceLocation id, JsonElement element, boolean isClient) throws Exception {
        if (Platform.isModLoaded("treechop")) {
            return false;
        }
        return super.load(id, element, isClient);
    }
}
