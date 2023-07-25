package smartin.miapi.modules.properties;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import com.redpxnda.nucleus.math.MathUtil;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;

import java.util.List;

import static smartin.miapi.item.modular.StatResolver.*;

public class ImmolateProperty extends DynamicCodecBasedProperty.IntermediateList<ImmolateProperty.Raw, ImmolateProperty.Holder> {
    public static final String KEY = "immolate";
    public static ImmolateProperty property;
    public static Codec<List<Raw>> codec = AutoCodec.of(Raw.class).codec().listOf();

    public ImmolateProperty() {
        super(KEY, codec, Raw::refine);
        property = this;

        ApplicationEvents.ENTITY_RELATED.startListening(
                (event, entity, stack, data, originals) -> onEntityEvent(event, stack, entity, (Holder) data, originals),
                ApplicationEvents.StackGetterHolder.ofMulti(
                        property::get,
                        list -> list.stream().map(h -> Pair.of(h.item, h)).toList()
                )
        );
    }

    public void onEntityEvent(ApplicationEvent<?, ?, ?> event, ItemStack stack, Entity entity, Holder holder, Object... originals) {
        //System.out.println(event.name + " the event");
        if (!(entity.getWorld() instanceof ServerWorld) || !event.equals(holder.event)) return;

        Entity target = ApplicationEvents.getEntityForTarget(holder.target, entity, event, originals);
        if (target == null) return;

        if (MathUtil.random(0d, 1d) < holder.actualChance)
            setOnFireFor(target, holder.actualDuration);
    }

    public static class Raw {
        public @AutoCodec.Optional DoubleFromStat chance = new DoubleFromStat(1);
        public @AutoCodec.Optional IntegerFromStat duration = new IntegerFromStat(20);
        public String item;
        public ApplicationEvent<?, ?, ?> event;
        public @AutoCodec.Optional String target = "this";

        public Holder refine(ItemModule.ModuleInstance modules) {
            Holder h = new Holder();
            h.item = item;
            h.event = event;
            h.target = target;
            h.actualChance = chance.evaluate(modules);
            h.actualDuration = duration.evaluate(modules);
            return h;
        }
    }
    public static class Holder extends Raw {
        public double actualChance;
        public int actualDuration;
    }

    public static void setOnFireFor(Entity entity, int ticks) {
        if (entity instanceof LivingEntity living) {
            ticks = ProtectionEnchantment.transformFireDuration(living, ticks);
        }
        if (entity.getFireTicks() < ticks) {
            entity.setFireTicks(ticks);
        }
    }
}
