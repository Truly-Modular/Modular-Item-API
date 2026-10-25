package smartin.miapi.modules.properties.potion;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.entity.uslot.EntityCacheSystem;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.ArrayList;
import java.util.List;


public class PassiveEffects extends CodecProperty<List<PossibleEffect>> {
    public static final ResourceLocation KEY = Miapi.id("passive_potion");
    public static PassiveEffects property = new PassiveEffects();
    public static Codec<List<PossibleEffect>> CODEC = Codec.list(PossibleEffect.CODEC);
    public static final EntityCacheSystem.EquipmentEntityCache<List<PossibleEffect>> CACHE =
            new EntityCacheSystem.EquipmentEntityCache<>(KEY, item -> property.getData(item.itemStack()).orElse(null));
    //public static EquipmentEffects<List<PossibleEffect>> effect = EquipmentEffect.register(KEY,(equipedItems->getAllPassiveEffects(equipedItems));

    protected PassiveEffects() {
        super(CODEC);

        MiapiEvents.LIVING_ENTITY_TICK_END.register(entity -> {
            if (entity.tickCount % (20 * 10) == 17) {
                //CACHE.getData(entity);
                PossibleEffect.apply(entity, i -> getData(i).orElse(new ArrayList<>()), entity.getRandom(), entity, entity);
            }
            return EventResult.pass();
        });
        setupLore();
    }

    public void dataLoaded(List<PossibleEffect> data) {
        data.forEach(effect -> CACHE.allowSlot(effect.group()));
    }

    public void setupLore() {
        LoreProperty.loreSuppliers.add((itemStack, tooltip, context, flag) -> {
            tooltip.addAll(PossibleEffect.getTooltip(
                    Component.translatable("miapi.potion.damaged.on_hit"),
                    getData(itemStack).orElse(new ArrayList<>()),
                    "miapi.potion.passive.tooltip",
                    "miapi.potion.passive.tooltip"));
        });
    }

    @Override
    public List<PossibleEffect> merge(List<PossibleEffect> left, List<PossibleEffect> right, MergeType mergeType) {
        return PossibleEffect.merge(left, right, mergeType);
    }

    @Override
    public List<PossibleEffect> initialize(List<PossibleEffect> effects, ModuleInstance module) {
        return effects.stream().map(e -> {
            PossibleEffect effect = e.initialize(e, module);
            effect = new PossibleEffect(
                    effect.potion(), effect.amplifier(),
                    new DoubleOperationResolvable(20 * 11), effect.ambient(),
                    effect.showParticle(), effect.showIcon(),
                    effect.probability(), effect.targetSelf(),
                    effect.group());
            return effect;
        }).toList();
    }
}
