package smartin.miapi.item.modular.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.items.projectile.ItemProjectileEntity;
import smartin.miapi.modules.properties.DisplayNameProperty;

public class ModularArrow extends ArrowItem implements ModularItem {
    public ModularArrow() {
        super(new Item.Settings().maxCount(64));
        //ItemTags.ARROWS
    }

    @Override
    public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
        stack = stack.copy();
        stack.setCount(1);
        return new ItemProjectileEntity(world, shooter, stack);
    }


    @Override
    public Text getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }
}
