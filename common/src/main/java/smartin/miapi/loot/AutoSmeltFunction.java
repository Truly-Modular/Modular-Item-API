package smartin.miapi.loot;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.mining.AutoSmeltProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Optional;

public class AutoSmeltFunction implements LootItemFunction {

    public static AutoCodec<AutoSmeltFunction> CODEC = AutoCodec.of(AutoSmeltFunction.class);

    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return RegistryInventory.autoSmeltFunctionLootItemFunctionType;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        if (lootContext.hasParam(LootContextParams.TOOL)) {
            if (AutoSmeltProperty.property.hasValue(lootContext.getParam(LootContextParams.TOOL))) {
                if (itemStack.isEmpty()) {
                    return itemStack;
                } else {
                    Optional<RecipeHolder<SmeltingRecipe>> optional = lootContext.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), lootContext.getLevel());
                    if (optional.isPresent()) {
                        ItemStack smeltStack = ((RecipeHolder) optional.get()).value().getResultItem(lootContext.getLevel().registryAccess());
                        if (!smeltStack.isEmpty()) {
                            return smeltStack.copyWithCount(smeltStack.getCount());
                        }
                    }

                    Miapi.LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", itemStack);
                    return itemStack;
                }
            }
        }
        return itemStack;
    }
}
