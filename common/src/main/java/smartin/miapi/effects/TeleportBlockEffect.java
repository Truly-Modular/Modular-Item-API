package smartin.miapi.effects;

import com.redpxnda.nucleus.util.Color;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class TeleportBlockEffect extends StatusEffect {

    public TeleportBlockEffect() {
        super(StatusEffectCategory.HARMFUL, Color.MAGENTA.hexInt());
    }
}
