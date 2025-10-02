package smartin.miapi.item.modular.items.bows;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;
import smartin.miapi.Miapi;
import smartin.miapi.entity.ItemProjectileEntity;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.FakeItemManager;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;

import java.util.List;

@NonnullDefault
public class ModularArrow extends ArrowItem implements PlatformModularItemMethods, ModularItem {
    public ModularArrow() {
        this(new Item.Properties().stacksTo(64));
    }

    public ModularArrow(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void verifyComponentsAfterLoad(ItemStack stack) {
        ComponentApplyProperty.initializeItemStack(stack, Miapi.registryAccess);
        super.verifyComponentsAfterLoad(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return FakeItemManager.getDefaultInstance(this);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, @Nullable LivingEntity shooter, @Nullable ItemStack weapon) {
        return new ItemProjectileEntity(level, shooter, ammo.copyWithCount(1), weapon);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MiapiEvents.INVENTORY_TICK.invoker().tick(stack,level,entity,slotId,isSelected);
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public Projectile asProjectile(Level world, Position position, ItemStack stack, Direction direction) {
        ItemStack itemStack = stack.copy();
        itemStack.setCount(1);
        ItemProjectileEntity arrowEntity = new ItemProjectileEntity(world, position, itemStack);
        arrowEntity.setPosRaw(position.x(), position.y(), position.z());
        arrowEntity.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrowEntity;
    }


    @Override
    public Component getName(ItemStack stack) {
        return DisplayNameProperty.getDisplayText(stack);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipType) {
        LoreProperty.appendLoreTop(itemStack, list, tooltipContext, tooltipType);
    }

    @Override
    public int getEnchantmentValue() {
        ItemStack itemStack = FakeItemManager.getLastInstance(this);
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
