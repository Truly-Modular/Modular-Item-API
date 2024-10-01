package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.events.ModularAttackEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class CopyItemOnHit extends CodecProperty<Holder<Item>> {
    public static ResourceLocation KEY = Miapi.id("copy_item_on_hit");
    public static CopyItemOnHit property;

    public CopyItemOnHit() {
        super(BuiltInRegistries.ITEM.holderByNameCodec());
        property = this;
        ModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            getData(itemStack).ifPresent(item -> {
                bonusDamage.add(item.value().getAttackDamageBonus(target, baseDamage, damageSource));
            });
            return EventResult.pass();
        });
        ModularAttackEvents.HURT_ENEMY.register((stack, target, attacker) -> {
            var optional = getData(stack);
            if (
                    optional.isPresent() &&
                    optional.get().value() instanceof Item item &&
                    !item.hurtEnemy(stack, target, attacker)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
        ModularAttackEvents.HURT_ENEMY_POST.register((stack, target, attacker) -> {
            var optional = getData(stack);
            if (
                    optional.isPresent() &&
                    optional.get().value() instanceof Item item) {
                item.postHurtEnemy(stack, target, attacker);
            }
            return EventResult.pass();
        });
    }

    @Override
    public Holder<Item> merge(Holder<Item> left, Holder<Item> right, MergeType mergeType) {
        return ModuleProperty.decideLeftRight(left, right, mergeType);
    }
}
