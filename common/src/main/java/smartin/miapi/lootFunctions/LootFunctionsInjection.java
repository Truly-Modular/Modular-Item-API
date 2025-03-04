package smartin.miapi.lootFunctions;

import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import smartin.miapi.Miapi;

import java.util.List;
import java.util.Optional;

public class LootFunctionsInjection {
    public static List<LootItemFunction> adjusted = List.of(
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
                    Optional.empty()),
            new ModuleSwapLootFunction(
                    Miapi.id("empty"),
                    1.0,
                    Optional.empty(),
                    Optional.empty()));
}
