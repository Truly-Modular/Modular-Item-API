package smartin.miapi.entity;

import com.google.common.collect.Iterables;
import com.redpxnda.nucleus.trinket.NucleusTrinket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EntityHelper {

    public static Iterable<ItemStack> getCurioItems(LivingEntity livingEntity) {
        try {
            return NucleusTrinket.getTrinketList(livingEntity);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    public static Iterable<ItemStack> getHandItems(LivingEntity livingEntity) {
        return livingEntity.getHandSlots();
    }

    public static Iterable<ItemStack> getEquipedNonHandItems(LivingEntity livingEntity) {
        return livingEntity.getArmorAndBodyArmorSlots();
    }


    public static Iterable<ItemStack> getAllEquipedItems(LivingEntity livingEntity) {
        return Iterables.concat(livingEntity.getAllSlots(), getCurioItems(livingEntity));
    }
}
