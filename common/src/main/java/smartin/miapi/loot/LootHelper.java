package smartin.miapi.loot;

import dev.architectury.event.events.common.LootEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import smartin.miapi.Miapi;

import java.util.List;
import java.util.Optional;

public class LootHelper {
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

    public static final ResourceLocation LOOT_TABLE_ID = Miapi.id("loot_table_id");
    public static final LootContextParam<ResourceLocation> LOOT_TABLE_PARAM = new LootContextParam<>(LOOT_TABLE_ID);

    public static void setup() {
        LootEvent.LootTableModificationContext lootTableModificationContext;
        LootItemFunction lootItemFunction;
    }
}
