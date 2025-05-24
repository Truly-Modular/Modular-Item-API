package smartin.miapi.forge;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import smartin.miapi.events.MiapiEvents;

import java.util.ArrayList;
import java.util.List;

public class MiapiGlobalLootModifier implements IGlobalLootModifier {
    public static MapCodec<MiapiGlobalLootModifier> CODEC = AutoCodec.of(MiapiGlobalLootModifier.class);

    @Override
    public ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> items, LootContext arg) {
        List<LootItemFunction> functions = new ArrayList<>();
        MiapiEvents.DEFAULT_LOOT_FUNCTIONS.invoker().adjust(functions);
        for (LootItemFunction function : functions) {
            items = ObjectArrayList.wrap(items.stream().map(i -> function.apply(i, arg)).toArray(ItemStack[]::new));
        }



        return items;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
