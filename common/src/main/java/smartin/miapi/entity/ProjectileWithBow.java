package smartin.miapi.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;

public interface ProjectileWithBow {
    EntityDataAccessor<ItemStack> BOW_ITEM_STACK = create();

    static EntityDataAccessor<ItemStack> get() {
        return BOW_ITEM_STACK;
    }

    ItemStack getBowItem();
    void setBowItem(ItemStack bowItem);

    static EntityDataAccessor<ItemStack> create() {
        return SynchedEntityData.defineId(Projectile.class, EntityDataSerializers.ITEM_STACK);
    }

}
