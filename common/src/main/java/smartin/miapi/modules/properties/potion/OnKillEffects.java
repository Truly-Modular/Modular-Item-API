package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;

/**
 * @header On Kill Effects Property
 * @path /data_types/properties/potion/on_kill_effects
 * @description_start
 * This property triggers specific potion effects on the attacker when a living entity they have hit is killed.
 * The effects are defined by `PossibleEffect` instances, which include parameters such as probability, duration,
 * and amplifier. These effects are applied to the attacker upon the death of the target entity.
 * The tooltip will display relevant information about these effects when the property is used in-game.
 * @description_end
 * @data effects: A list of `PossibleEffect` instances that define the potion effects to be applied when the attacker successfully kills an entity.
 */

public class OnKillEffects extends CodecProperty<List<PossibleEffect>> {
    public static final ResourceLocation KEY = Miapi.id("on_kill_potion");
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
