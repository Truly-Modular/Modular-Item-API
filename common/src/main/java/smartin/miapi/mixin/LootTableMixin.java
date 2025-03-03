package smartin.miapi.mixin;

import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LootTable.class)
public class LootTableMixin {
    /*
    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParamSet;Ljava/util/Optional;Ljava/util/List;Ljava/util/List;)V",
            at = @At(value = "HEAD"), ordinal = 0)
    private static List<LootItemFunction> miapi$adjustLootTable(List<LootItemFunction> function) {
        List<LootItemFunction> adjusted = new ArrayList<>(function);
        adjusted.add(
                new MaterialSwapLootFunction(
                        Miapi.id("empty"),
                        -3.0,
                        0.5,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        Optional.empty(),
                        Optional.empty()));
        adjusted.add(new ModuleSwapLootFunction(
                Miapi.id("empty"),
                1.0,
                Optional.empty(),
                Optional.empty()));
        return adjusted;
    }

     */
}
