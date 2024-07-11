package smartin.miapi.item.modular.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import smartin.miapi.item.FakeItemstackReferenceProvider;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.PlatformModularItemMethods;
import smartin.miapi.modules.properties.DisplayNameProperty;
import smartin.miapi.modules.properties.LoreProperty;
import smartin.miapi.modules.properties.enchanment.EnchantAbilityProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;

import java.util.List;

public class ExampleModularItem extends Item implements PlatformModularItemMethods, ModularItem {
    public static Item modularItem;

    public ExampleModularItem() {
        super(new Item.Properties().stacksTo(1).durability(50));
        modularItem = this;
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
