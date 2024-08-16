package smartin.miapi.modules.properties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property adds a passive Food drain similar to sprinting
 */
public class ExhaustionProperty extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("food_exhaustion");
    public static ExhaustionProperty property;

    public ExhaustionProperty() {
        super(KEY);
        property = this;
    }

    public static void step(Entity entity){
        if (entity instanceof Player playerEntity && playerEntity.isControlledByLocalInstance()) {
            double getValue = property.getForItems(playerEntity.getArmorSlots());
            if (playerEntity.getRandom().nextDouble() < 0.05 && getValue > 0.2) {
                playerEntity.getFoodData().addExhaustion((float) (getValue / 100.0f));
            }
        }
    }
}
