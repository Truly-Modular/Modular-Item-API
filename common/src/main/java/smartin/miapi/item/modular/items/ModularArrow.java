package smartin.miapi.item.modular.items;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.List;

public class ModularArrow extends ArrowItem implements PlatformModularItemMethods,ModularItem {
    public ModularArrow() {
        this(new Item.Settings().maxCount(64));
    }

    public ModularArrow(Item.Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new ProjectileDispenserBehavior() {
            @Override
            protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                ItemStack itemStack = stack.copy();
                itemStack.setCount(1);
                ItemProjectileEntity arrowEntity = new ItemProjectileEntity(world, position, itemStack);
                arrowEntity.setPos(position.getX(), position.getY(), position.getZ());
                arrowEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                return arrowEntity;
            }
        });
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

    @Override
    public void appendTooltip(ItemStack itemStack, TooltipContext tooltipContext, List<Text> list, TooltipType tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }
}
