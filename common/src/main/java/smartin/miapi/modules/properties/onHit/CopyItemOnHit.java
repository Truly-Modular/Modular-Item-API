package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.events.MeleeModularAttackEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

/**
 * @header Copy On Hit
 * @path /data_types/properties/on_hit/copy_from_item
 * @description_start Attempts to copy on hit effects from item, is implemented to work with onhits implemented like the maces
 * {
 *     "copy_item_on_hit":["minecraft:mace"]
 * }
 * This copies onhit effects from used weapons.
 * @description_end
 */
public class CopyItemOnHit extends CodecProperty<List<Holder<Item>>> {
    public static ResourceLocation KEY = Miapi.id("copy_item_on_hit");
    public static CopyItemOnHit property;

    public CopyItemOnHit() {
        super(Miapi.toListOrSimple(BuiltInRegistries.ITEM.holderByNameCodec()));
        property = this;
        MeleeModularAttackEvents.ATTACK_DAMAGE_BONUS.register((target, itemStack, baseDamage, damageSource, bonusDamage) -> {
            getData(itemStack).ifPresent(list -> {
                list.forEach(item -> {
                    bonusDamage.add(item.value().getAttackDamageBonus(target, baseDamage, damageSource));
                });
            });
            return EventResult.pass();
        });
        MeleeModularAttackEvents.HURT_ENEMY.register((stack, target, attacker) -> {
            var optional = getData(stack);
            if (
                    optional.isPresent()) {
                for (var holder : optional.get()) {
                    if (holder.value() instanceof Item item &&
                        !item.hurtEnemy(stack, target, attacker)) {
                        return EventResult.interruptFalse();
                    }
                }
            }
            return EventResult.pass();
        });
        MeleeModularAttackEvents.HURT_ENEMY_POST.register((stack, target, attacker) -> {
            var optional = getData(stack);
            if (
                    optional.isPresent()) {
                for (var holder : optional.get()) {
                    if (holder.value() instanceof Item item) {
                        item.postHurtEnemy(stack, target, attacker);
                    }
                }
            }
            return EventResult.pass();
        });
    }

    @Override
    public List<Holder<Item>> merge(List<Holder<Item>> left, List<Holder<Item>> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}
