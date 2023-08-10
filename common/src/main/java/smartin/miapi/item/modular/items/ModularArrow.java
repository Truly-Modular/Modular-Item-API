package smartin.miapi.item.modular.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.abilities.util.ItemProjectile.ItemProjectile;

public class ModularArrow extends ArrowItem implements ModularItem {
    public ModularArrow() {
        super(new Item.Settings().maxCount(64));
        //ItemTags.ARROWS
    }

    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        stack = stack.copy();
        stack.setCount(1);
        return new ItemProjectile(world, shooter, stack);
    }
}
