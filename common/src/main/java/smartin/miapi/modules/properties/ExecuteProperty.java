package smartin.miapi.modules.properties;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.datapack.codec.AutoCodec;
import com.redpxnda.nucleus.math.MathUtil;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import smartin.miapi.events.property.ApplicationEvent;
import smartin.miapi.events.property.ApplicationEvents;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.DynamicCodecBasedProperty;

import java.util.List;

import static smartin.miapi.item.modular.StatResolver.DoubleFromStat;
import static smartin.miapi.item.modular.StatResolver.IntegerFromStat;

public class ExecuteProperty extends DynamicCodecBasedProperty.IntermediateList<ExecuteProperty.Raw, ExecuteProperty.Holder> {
    public static final String KEY = "previewStack";
    public static ExecuteProperty property;
    public static Codec<List<Raw>> codec = AutoCodec.of(Raw.class).codec().listOf();

    public ExecuteProperty() {
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
        if (!(entity.getWorld() instanceof ServerWorld) || !event.equals(holder.event) || event.equals(ApplicationEvents.HURT)) return; // hurt event does weird stuff, only use hurt_after

        // 'entity' is considered to be the attacker. They are the holder of the modular item with this effect. If they are not a living entity, the "generic kill" damage source will be used.
        // todo test ^ that logic (in multiplayer- make sure the death message is actually correct)

        Entity target = ApplicationEvents.getEntityForTarget(holder.target, entity, event, originals);
        if (!(target instanceof LivingEntity living) || !living.isAlive()) return;

        DamageSource source = target.getDamageSources().genericKill();
        if (entity instanceof PlayerEntity player)
            source = target.getDamageSources().playerAttack(player);
        else if (entity instanceof LivingEntity livingAttacker)
            source = target.getDamageSources().mobAttack(livingAttacker);

        double health = holder.inPercent ? holder.actualHealth/100 * living.getMaxHealth() : holder.actualHealth;
        if (MathUtil.random(0d, 1d) < holder.actualChance && living.getHealth() <= health)
            living.damage(source, Float.MAX_VALUE);
    }

    @AutoCodec.Settings(optionalByDefault = true)
    public static class Raw {
        public DoubleFromStat chance = new DoubleFromStat(1);
        public @AutoCodec.Mandatory DoubleFromStat health;
        public boolean inPercent = false;
        public String item = "attacker.mainhand";
        public ApplicationEvent<?, ?, ?> event = ApplicationEvents.HURT_AFTER;
        public String target = "victim";

        public Holder refine(ItemModule.ModuleInstance modules) {
            Holder h = new Holder();
            h.item = item;
            h.event = event;
            h.target = target;
            h.inPercent = inPercent;
            h.actualChance = chance.evaluate(modules);
            h.actualHealth = health.evaluate(modules);
            return h;
        }
    }
    public static class Holder extends Raw {
        public double actualChance;
        public double actualHealth;
    }
}
