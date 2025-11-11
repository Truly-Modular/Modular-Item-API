package smartin.miapi.loot;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.LootEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.mixin.loot.LootTableAccessor;

import java.util.List;
import java.util.Optional;

public class LootHelper {
    public static List<LootItemFunction> adjusted = List.of(
            new MaterialSwapLootFunction(
                    Miapi.id("empty"),
                    -3.5,
                    0.4,
                    1.0,
                    0.2,
                    1.0,
                    1.0,
                    1.0,
                    Optional.empty(),
                    Optional.empty(), false),
            new ModuleSwapLootFunction(
                    Miapi.id("empty"),
                    1.0,
                    Optional.empty(),
                    Optional.empty(), false));

    public static final ResourceLocation LOOT_TABLE_ID = Miapi.id("loot_table_id");
    public static final LootContextParam<ResourceLocation> LOOT_TABLE_PARAM = new LootContextParam<>(LOOT_TABLE_ID);

    public static void setup() {
        MiapiEvents.DEFAULT_LOOT_FUNCTIONS.register(list -> {
            //list.add(new AutoSmeltFunction());
            list.add(new GlobalLootModularisationFunction());
            return EventResult.pass();
        });
        if (true) {
            return;
        }
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new LootModifierManager(), Miapi.id("global_loot"));
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            LootModifierManager.getLootPools().forEach(lootTable -> {
                ((LootTableAccessor) lootTable).getPools().forEach(lootPool -> {
                    LootPool.lootPool()
                            .setRolls(lootPool.rolls);
                    context.addPool(getBuilderFromLootPool(lootPool));
                });
            });
        });
    }

    public static LootPool.Builder getBuilderFromLootPool(LootPool lootPool) {
        LootPool.Builder builder = new LootPool.Builder()
                .setRolls(lootPool.rolls)
                .setBonusRolls(lootPool.bonusRolls);


        // Add entries
        for (LootPoolEntryContainer entry : lootPool.entries) {
            builder.add(new DummyBuilder(entry)); // Assuming add() accepts a built entry
        }

        // Add conditions
        for (LootItemCondition condition : lootPool.conditions) {
            builder.when(() -> condition); // Assuming when() accepts a built condition
        }

        // Add functions
        for (LootItemFunction function : lootPool.functions) {
            builder.apply(() -> function); // Assuming apply() accepts a built function
        }

        return builder;
    }

    static class DummyBuilder extends LootPoolSingletonContainer.Builder<DummyBuilder> {
        public LootPoolEntryContainer entry;

        public DummyBuilder(LootPoolEntryContainer entry) {
            this.entry = entry;
        }

        protected DummyBuilder getThis() {
            return this;
        }

        public LootPoolEntryContainer build() {
            return entry;
        }
    }
}
