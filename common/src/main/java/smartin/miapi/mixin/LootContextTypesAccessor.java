package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

@Mixin(LootContextParamSets.class)
public interface LootContextTypesAccessor {
    @Invoker("register")
    static LootContextParamSet register(String name, Consumer<LootContextParamSet.Builder> type) {
        throw new AssertionError();
    }
}
