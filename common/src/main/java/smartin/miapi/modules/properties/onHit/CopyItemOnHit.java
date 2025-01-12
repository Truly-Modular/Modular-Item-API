package smartin.miapi.modules.properties.onHit;

import dev.architectury.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.Miapi;
import smartin.miapi.events.ModularAttackEvents;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

/**
 * @header Copy On Hit
 * @path /data_types/properties/on_hit/copy_from_item
 * @description_start Attempts to copy on hit effects from item, is implemented to work with onhits implemented like the maces
 * @description_end
 * @data copy_item_on_hit:the id of the item to copy
 */
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
            Miapi.LOGGER.info("post hit for item " + optional);
            if (
                    optional.isPresent() &&
                    optional.get().value() instanceof Item item) {
                Miapi.LOGGER.info("post hit for item " + item);
                item.postHurtEnemy(stack, target, attacker);
            }
            return EventResult.pass();
        });
    }

    @Override
    public Holder<Item> merge(Holder<Item> left, Holder<Item> right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
