package smartin.miapi.modules.properties.potion;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
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
            if (!entity.getWorld().isClient()) {
                if (source.getAttacker() instanceof LivingEntity livingEntity) {
                    applyEffects(livingEntity, livingEntity, livingEntity, this::isTargetOther);
                }
            }
            return EventResult.pass();
        }));

        setupLore();
    }

    public void setupLore(){
        LoreProperty.loreSuppliers.add((ItemStack itemStack, @Nullable World world, List<Text> tooltip, TooltipContext context) -> {
            List<Text> lines = new ArrayList<>();
            for (EffectHolder effectHolder : merge(getStatusEffects(itemStack))) {
                if (effectHolder.isGuiVisibility()) {
                    Text text = effectHolder.getPotionDescription();
                    lines.add(Text.translatable("miapi.potion.kill.tooltip", text, effectHolder.getDurationSeconds(), effectHolder.getAmplifier()));
                }
            }
            if (!lines.isEmpty()) {
                lines.add(0, Text.translatable("miapi.potion.kill.header").getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)).get(0));
                lines.add(0, Text.empty());
            }
            tooltip.addAll(lines);
        });
    }

    public boolean isTargetOther(EffectHolder holder, EquipmentSlot slot) {
        return true;
    }
}
