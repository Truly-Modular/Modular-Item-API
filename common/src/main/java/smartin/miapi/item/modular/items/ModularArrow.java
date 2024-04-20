package smartin.miapi.item.modular.items;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.RarityProperty;

import java.util.List;

public class ModularArrow extends ArrowItem implements ModularItem {
    public ModularArrow() {
        this(new Settings().maxCount(64));
    }

    public ModularArrow(Settings settings) {
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
    public Rarity getRarity(ItemStack stack) {
        return RarityProperty.getRarity(stack);
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        LoreProperty.appendLoreTop(stack, world, tooltip, context);
    }
}
