package smartin.miapi.modules.properties.potion;

import dev.architectury.event.EventResult;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class OnDamagedEffects extends PotionEffectProperty {
    public static String KEY = "on_hurt_potion";
    public OnDamagedEffects property;

    public OnDamagedEffects() {
        super(KEY);
        property = this;
        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (!listener.livingEntity.level().isClientSide()) {
                applyEffects(listener.livingEntity, listener.livingEntity, listener.getCausingItemStack(), listener.livingEntity, this::isTargetSelf);
                if (listener.damageSource.getEntity() instanceof LivingEntity attacker) {
                    applyEffects(attacker, listener.livingEntity, listener.getCausingItemStack(), attacker, this::isTargetOther);
                }
            }
            return EventResult.pass();
        });

        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((ItemStack itemStack, List<Component> tooltip, Item.TooltipContext context, TooltipFlag tooltipType) -> {
            List<Component> lines = new ArrayList<>();
            for (EffectHolder effectHolder : merge(getStatusEffects(itemStack))) {
                if (effectHolder.isGuiVisibility()) {
                    Component text = effectHolder.getPotionDescription();
                    if (isTargetSelf(effectHolder)) {
                        lines.add(Component.translatable("miapi.potion.damaged.self.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    } else {
                        lines.add(Component.translatable("miapi.potion.damaged.other.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                    }
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Component.translatable("miapi.potion.damaged.on_hit").toFlatList(Style.EMPTY.withColor(ChatFormatting.GRAY)).get(0));
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
