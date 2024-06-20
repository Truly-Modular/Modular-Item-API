package smartin.miapi.modules.properties;

import com.redpxnda.nucleus.math.MathUtil;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.events.MiapiProjectileEvents;
import smartin.miapi.item.FakeEnchantment;
import smartin.miapi.modules.properties.util.DoubleProperty;

/**
 * This property is arson.
 */
public class ImmolateProperty extends DoubleProperty {
    public static final String KEY = "immolate";
    public static ImmolateProperty property;


    public ImmolateProperty() {
        super(KEY);
        property = this;
        FakeEnchantment.addTransformer(Enchantments.FIRE_PROTECTION, (stack, level) -> {
            int toLevel = (int) Math.ceil(getValueSafe(stack) / 4) + level;
            return Math.min(toLevel + level, Math.max(4, level));
        });
        TickEvent.PLAYER_POST.register(player -> {
            if(player.getWorld().isClient()){
                return;
            }
            if (player.age % 250 == 0) {
                double strength = getForItems(player.getItemsEquipped());
                if (strength > 0) {
                    double chance = Math.min(1, strength * 0.05 + 0.05);
                    if (MathUtil.random(0d, 1d) < chance) {
                        double ticksExtention = strength * 2;
                        int fireTicks = (int) Math.ceil(MathUtil.random(50 + ticksExtention, 80 + ticksExtention * 1.5));
                        setOnFireFor(player, fireTicks);
                    }
                }
            }
        });
        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> {
            if(player.getWorld().isClient()){
                return EventResult.pass();
            }
            double strength = getForItems(player.getHandItems());
            if (strength > 0) {
                double chance = Math.min(1, strength * 0.05 + 0.2);
                if (MathUtil.random(0d, 1d) < chance) {
                    double ticksExtention = strength * 2;
                    int fireTicks = (int) Math.ceil(MathUtil.random(50 + ticksExtention, 80 + ticksExtention * 1.5));
                    setOnFireFor(player, fireTicks);
                }
                if (!target.isFireImmune()) {
                    double ticksExtention = strength * 2;
                    int fireTicks = (int) Math.ceil(MathUtil.random(50 + ticksExtention, 80 + ticksExtention * 1.5));
                    setOnFireFor(target, fireTicks);
                }
            }
            return EventResult.pass();
        });
        BlockEvent.BREAK.register(((level, pos, state, player, xp) -> {
            if(player.getWorld().isClient()){
                return EventResult.pass();
            }
            double strength = getForItems(player.getHandItems());
            if (strength > 0) {
                double chance = Math.min(1, strength * 0.05 + 0.2);
                if (MathUtil.random(0d, 1d) < chance) {
                    double ticksExtention = strength * 2;
                    int fireTicks = (int) Math.ceil(MathUtil.random(50 + ticksExtention, 80 + ticksExtention * 1.5));
                    setOnFireFor(player, fireTicks);
                }
            }
            return EventResult.pass();
        }));
        MiapiProjectileEvents.MODULAR_PROJECTILE_ENTITY_HIT.register((modularProjectileEntityHitEvent) -> {
            if(modularProjectileEntityHitEvent.projectile.getWorld().isClient()){
                return EventResult.pass();
            }
            double strength = getValueSafe(modularProjectileEntityHitEvent.projectile.asItemStack());
            ItemStack bowItem = modularProjectileEntityHitEvent.projectile.getBowItem();
            if (bowItem != null && !bowItem.isEmpty()) {
                strength += getValueSafe(modularProjectileEntityHitEvent.projectile.getBowItem());
            }
            if(strength>0){
                double chance = Math.min(1, strength * 0.1 + 0.4);
                if (MathUtil.random(0d, 1d) < chance) {
                    Entity entity = modularProjectileEntityHitEvent.entityHitResult.getEntity();
                    if (entity instanceof LivingEntity living && !living.isFireImmune()) {
                        double ticksExtention = strength * 2;
                        int fireTicks = (int) Math.ceil(MathUtil.random(50 + ticksExtention, 80 + ticksExtention * 1.5));
                        setOnFireFor(living, fireTicks);
                    }
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public Double getValue(ItemStack stack) {
        return getValueRaw(stack);
    }

    @Override
    public double getValueSafe(ItemStack stack) {
        return getValueSafeRaw(stack);
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
