package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class OnDamagedEffects extends CodecProperty<List<PossibleEffect>> {
    public static final ResourceLocation KEY = Miapi.id("on_attack_potion");
    public OnDamagedEffects property;
    public static Codec<List<PossibleEffect>> CODEC = Codec.list(PossibleEffect.CODEC);

    public OnDamagedEffects() {
        super(CODEC);
        property = this;

        MiapiEvents.LIVING_HURT.register((listener) -> {
            if (listener.damageSource.getEntity() instanceof LivingEntity attacker && !attacker.level().isClientSide()) {
                LivingEntity defender = listener.livingEntity;
                PossibleEffect.applyEffects(defender, attacker, attacker, i -> getData(i).orElse(new ArrayList<>()));
            }
            return EventResult.pass();
        });
        setupLore();
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, flag) -> {
            tooltip.addAll(PossibleEffect.getTooltip(
                    Component.translatable("miapi.potion.target.on_hit"),
                    getData(itemStack).orElse(new ArrayList<>()),
                    "miapi.potion.target.other.tooltip",
                    "miapi.potion.target.self.tooltip"));
        });
    }

    @Override
    public List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        return PossibleEffect.merge(left, right, mergeType);
    }
}
