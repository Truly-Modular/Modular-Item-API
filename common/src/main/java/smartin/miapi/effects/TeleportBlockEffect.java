package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class TeleportBlockEffect extends MobEffect {

    public TeleportBlockEffect() {
        super(MobEffectCategory.HARMFUL, Color.MAGENTA.hexInt());
    }
}
