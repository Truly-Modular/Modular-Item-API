package smartin.miapi.modules.properties.potion;

import dev.architectury.event.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
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
            if (listener.damageSource.getEntity() instanceof LivingEntity livingEntity && !livingEntity.level().isClientSide()) {
                applyEffects(listener.livingEntity, livingEntity, listener.getCausingItemStack(), livingEntity, this::isTargetOther);
                applyEffects(livingEntity, livingEntity, listener.getCausingItemStack(), livingEntity, this::isTargetSelf);
            }
            return EventResult.pass();
        });
        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipContext context)-> {
            List<Component> lines = new ArrayList<>();
            for (EffectHolder effectHolder : merge(getStatusEffects(itemStack))) {
                if (effectHolder.isGuiVisibility()) {
                    Component text = effectHolder.getPotionDescription();
                    if (isTargetSelf(effectHolder)) {
                        lines.add(Component.translatable("miapi.potion.target.self.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    } else {
                        lines.add(Component.translatable("miapi.potion.target.other.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    }
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Component.translatable("miapi.potion.target.on_hit").toFlatList(Style.EMPTY.withColor(ChatFormatting.GRAY)).get(0));
                lines.add(0, Component.empty());
            }
            tooltip.addAll(lines);
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
