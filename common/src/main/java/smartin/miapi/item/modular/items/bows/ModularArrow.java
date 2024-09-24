package smartin.miapi.item.modular.items.bows;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

public class ModularArrow extends ArrowItem implements PlatformModularItemMethods, ModularItem {
    public ModularArrow() {
        this(new Item.Properties().stacksTo(64));
    }

    public ModularArrow(Item.Properties settings) {
        super(settings);
    }


    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack ammo, @Nullable LivingEntity shooter, @Nullable ItemStack weapon) {
        return new ItemProjectileEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level world, @NotNull Position position, @NotNull ItemStack stack, @NotNull Direction direction) {
        ItemStack itemStack = stack.copy();
        itemStack.setCount(1);
        ItemProjectileEntity arrowEntity = new ItemProjectileEntity(world, position, itemStack);
        arrowEntity.setPosRaw(position.x(), position.y(), position.z());
        arrowEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrowEntity;
    }


    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull TooltipContext tooltipContext, @NotNull List<Component> list, @NotNull TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemstackReferenceProvider.getFakeReference(this);
        if (itemStack != null) {
            return (int) EnchantAbilityProperty.getEnchantAbility(itemStack);
        }
        return 15;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return MiningLevelProperty.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return MiningLevelProperty.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return MiningLevelProperty.isCorrectToolForDrops(stack, state);
    }
}
