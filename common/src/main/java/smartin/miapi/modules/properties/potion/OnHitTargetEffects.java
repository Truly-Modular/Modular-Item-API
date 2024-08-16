package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

public class OnHitTargetEffects extends CodecProperty<List<PossibleEffect>> {
    public static final ResourceLocation KEY = Miapi.id("on_hit_potion");
    public OnHitTargetEffects property;
    public static Codec<List<PossibleEffect>> CODEC = Codec.list(PossibleEffect.CODEC);

    public OnHitTargetEffects() {
        super(CODEC);
        property = this;

        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getEntity() instanceof LivingEntity attacker && !attacker.level().isClientSide()) {
                LivingEntity defender = listener.livingEntity;
                PossibleEffect.applyEffects(attacker, defender, defender, i -> getData(i).orElse(new ArrayList<>()));
            }
            return EventResult.pass();
        });
        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, flag) -> {
            tooltip.addAll(PossibleEffect.getTooltip(
                    Component.translatable("miapi.potion.damaged.on_hit"),
                    getData(itemStack).orElse(new ArrayList<>()),
                    "miapi.potion.damaged.other.tooltip",
                    "miapi.potion.damaged.self.tooltip"));
        });
    }

    @Override
    public List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        return PossibleEffect.merge(left, right, mergeType);
    }
}
