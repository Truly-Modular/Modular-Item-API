package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CodecBasedProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

public class OnKillEffects extends CodecBasedProperty<List<PossibleEffect>> {
    public static String KEY = "on_kill_potion";
    public OnKillEffects property;
    public static Codec<List<PossibleEffect>> CODEC = Codec.list(PossibleEffect.CODEC);

    public OnKillEffects() {
        super(CODEC);
        property = this;
        EntityEvent.LIVING_DEATH.register(((entity, source) -> {
            if (!entity.level().isClientSide()) {
                if (source.getEntity() instanceof LivingEntity livingEntity) {
                    PossibleEffect.applyEffects(livingEntity, livingEntity, i -> getData(i).orElse(new ArrayList<>()));
                }
            }
            return EventResult.pass();
        }));

        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, flag) -> {
            tooltip.addAll(PossibleEffect.getTooltip(
                    Component.translatable("miapi.potion.kill.header"),
                    getData(itemStack).orElse(new ArrayList<>()),
                    "miapi.potion.kill.tooltip",
                    "miapi.potion.kill.tooltip"));
        });
    }

    @Override
    public List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        return PossibleEffect.merge(left, right, mergeType);
    }
}
