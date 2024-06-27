package smartin.miapi.modules.properties.potion;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.ArrayList;
import java.util.List;

public class OnKillEffects extends PotionEffectProperty {
    public static String KEY = "on_kill_potion";
    public OnKillEffects property;

    public OnKillEffects() {
        super(KEY);
        property = this;
        EntityEvent.LIVING_DEATH.register(((entity, source) -> {
            if (!entity.level().isClientSide()) {
                if (source.getEntity() instanceof LivingEntity livingEntity) {
                    applyEffects(livingEntity, livingEntity, livingEntity, this::isTargetOther);
                }
            }
            return EventResult.pass();
        }));

        setupLore();
    }

    public void setupLore(){
        LoreProperty.loreSuppliers.add((ItemStack itemStack, @Nullable Level world, List<Component> tooltip, TooltipContext context) -> {
            List<Component> lines = new ArrayList<>();
            for (EffectHolder effectHolder : merge(getStatusEffects(itemStack))) {
                if (effectHolder.isGuiVisibility()) {
                    Component text = effectHolder.getPotionDescription();
                    lines.add(Component.translatable("miapi.potion.kill.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Component.translatable("miapi.potion.kill.header").toFlatList(Style.EMPTY.withColor(ChatFormatting.GRAY)).get(0));
                lines.add(0, Component.empty());
            }
            tooltip.addAll(lines);
        });
    }

    public boolean isTargetOther(EffectHolder holder, EquipmentSlot slot) {
        return true;
    }
}
