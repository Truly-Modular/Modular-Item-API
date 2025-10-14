package smartin.miapi.modules.properties.onHit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

public class ComboTimeProperty extends DoubleProperty {
    public static ResourceLocation KEY = Miapi.id("combo_time");
    public static ComboTimeProperty property;

    public ComboTimeProperty() {
        super(KEY);
        property = this;
    }

    public int getTicks(ItemStack itemStack) {
        return getData(itemStack).map(d -> d.evaluate(0.0, 20)).orElse(20.0).intValue();
    }
}
