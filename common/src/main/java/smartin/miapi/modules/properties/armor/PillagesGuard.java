package smartin.miapi.modules.properties.armor;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.onHit.entity.IllagerBane;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * decreases damage from raid type mobs
 * @header PillagesGuard Property
 * @description_start
 * This property allows you to have reduced damage intake from pillagers/raid related mobs
 * @desciption_end
 * @path /data_types/properties/armor/pillager_guard
 * @data pillager_guard:a Double Resolvable
 */
public class PillagesGuard extends DoubleProperty {
    public static final ResourceLocation KEY = Miapi.id("pillager_guard");
    public static PillagesGuard property;


    public PillagesGuard() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((livingHurtEvent) -> {
            if (livingHurtEvent.damageSource.getEntity() instanceof LivingEntity living) {
                if (IllagerBane.isIllagerType(living) && !living.level().isClientSide()) {
                    double level = 1;
                    for (ItemStack itemStack : livingHurtEvent.livingEntity.getArmorSlots()) {
                        level -= (1 - valueRemap(getValue(itemStack).orElse(0.0)));
                    }
                    livingHurtEvent.amount *= (float) level;
                }
            }
            return EventResult.pass();
        });
    }

    public static double valueRemap(double x) {
        return 1 - (2 / (1 + Math.exp(-x / 10)) - 1);
    }
}
