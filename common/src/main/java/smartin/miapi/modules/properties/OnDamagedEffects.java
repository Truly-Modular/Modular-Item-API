package smartin.miapi.modules.properties;

import dev.architectury.event.EventResult;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class OnDamagedEffects extends PotionEffectProperty {
    public static String KEY = "on_hurt_potion";
    public OnDamagedEffects property;

    public OnDamagedEffects() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getAttacker() instanceof LivingEntity livingEntity && !livingEntity.getWorld().isClient()) {
                applyEffects(livingEntity, listener.livingEntity, livingEntity, this::isTargetOther);
            }
            if (listener.damageSource.getAttacker() instanceof LivingEntity livingEntity && !livingEntity.getWorld().isClient()) {
                applyEffects(listener.livingEntity, listener.livingEntity, livingEntity, this::isTargetSelf);
            }
            return EventResult.pass();
        });

        LoreProperty.loreSuppliers.add(itemStack -> {
            List<Text> lines = new ArrayList<>();
            for (EffectHolder effectHolder : getStatusEffects(itemStack)) {
                if (effectHolder.isGuiVisibility()) {
                    Text text = effectHolder.getPotionDescription();
                    if (isTargetSelf(effectHolder)) {
                        lines.add(Text.translatable("miapi.potion.damaged.self.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    } else {
                        lines.add(Text.translatable("miapi.potion.damaged.other.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    }
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Text.translatable("miapi.potion.damaged.on_hit").getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)).get(0));
                lines.add(0,Text.empty());
            }
            return lines;
        });
    }

    public boolean isTargetOther(EffectHolder holder, EquipmentSlot slot) {
        return isTargetOther(holder);
    }

    public boolean isTargetOther(EffectHolder holder) {
        return !ModuleProperty.getBoolean(holder.rawData(), "target_self", holder.moduleInstance(), false);
    }

    public boolean isTargetSelf(EffectHolder holder, EquipmentSlot slot) {
        return !isTargetOther(holder);
    }

    public boolean isTargetSelf(EffectHolder holder) {
        return !isTargetOther(holder);
    }
}
