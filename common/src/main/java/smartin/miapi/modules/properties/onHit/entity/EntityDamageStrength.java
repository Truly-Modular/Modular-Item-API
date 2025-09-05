package smartin.miapi.modules.properties.onHit.entity;

import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableFloat;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;

public class EntityDamageStrength extends GenericEntityStrengthProperty {
    public static final ResourceLocation KEY = Miapi.id("entity_damage");

    public EntityDamageStrength() {
        super();
        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register(new MeleeModularAttackEvents.GetAttackDamageBonus() {
            @Override
            public EventResult getAttackDamageBonus(Entity target, ItemStack itemStack, float baseDamage, DamageSource damageSource, MutableFloat bonusDamage) {
                bonusDamage.add(strengthForEntity(target.getType().arch$holder(), itemStack));
                return EventResult.pass();
            }
        });
    }

    @Override
    public Component getFallbackName(EntityType<?> firstType) {
        return Component.translatable("miapi.property.entity.attack.default",firstType.getDescription());
    }

    @Override
    public Component getBaseDescription(double strength) {
        return Component.translatable("miapi.property.entity.attack.description");
    }
}
