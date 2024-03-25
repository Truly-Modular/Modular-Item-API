package smartin.miapi.modules.properties.potion;

import dev.architectury.event.EventResult;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class OnHitTargetEffects extends PotionEffectProperty {
    public static String KEY = "on_attack_potion";
    public OnHitTargetEffects property;

    public OnHitTargetEffects() {
        super(KEY);
        property = this;

        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getAttacker() instanceof LivingEntity livingEntity && !livingEntity.getWorld().isClient()) {
                applyEffects(listener.livingEntity, livingEntity, listener.getCausingItemStack(), livingEntity, this::isTargetOther);
                applyEffects(livingEntity, livingEntity, listener.getCausingItemStack(), livingEntity, this::isTargetSelf);
            }
            return EventResult.pass();
        });
        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add(itemStack -> {
            List<Text> lines = new ArrayList<>();
            for (EffectHolder effectHolder : merge(getStatusEffects(itemStack))) {
                if (effectHolder.isGuiVisibility()) {
                    Text text = effectHolder.getPotionDescription();
                    if (isTargetSelf(effectHolder)) {
                        lines.add(Text.translatable("miapi.potion.target.self.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    } else {
                        lines.add(Text.translatable("miapi.potion.target.other.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    }
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Text.translatable("miapi.potion.target.on_hit").getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)).get(0));
                lines.add(0, Text.empty());
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
