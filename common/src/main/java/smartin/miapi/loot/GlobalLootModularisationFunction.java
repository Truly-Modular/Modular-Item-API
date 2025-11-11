package smartin.miapi.loot;

import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.registries.RegistryInventory;

public class GlobalLootModularisationFunction implements LootItemFunction {

    public static AutoCodec< GlobalLootModularisationFunction> CODEC = AutoCodec.of( GlobalLootModularisationFunction.class);

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return RegistryInventory.globalLootItemFunctionType;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        for (LootItemFunction function : LootHelper.adjusted) {
            itemStack = function.apply(itemStack,lootContext);
        }
        return itemStack;
    }
}
