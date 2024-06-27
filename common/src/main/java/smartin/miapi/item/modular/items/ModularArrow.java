package smartin.miapi.item.modular.items;

import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;

import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ModularArrow extends ArrowItem implements PlatformModularItemMethods,ModularItem {
    public ModularArrow() {
        this(new Item.Properties().stacksTo(64));
    }

    public ModularArrow(Item.Properties settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, new ProjectileDispenseBehavior() {
            @Override
            protected Projectile createProjectile(Level world, Position position, ItemStack stack) {
                ItemStack itemStack = stack.copy();
                itemStack.setCount(1);
                ItemProjectileEntity arrowEntity = new ItemProjectileEntity(world, position, itemStack);
                arrowEntity.setPosRaw(position.x(), position.y(), position.z());
                arrowEntity.pickup = AbstractArrow.Pickup.ALLOWED;
                return arrowEntity;
            }
        });
        //ItemTags.ARROWS
    }


    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        stack = stack.copy();
        stack.setCount(1);
        return new ItemProjectileEntity(world, shooter, stack);
    }


    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }
}
