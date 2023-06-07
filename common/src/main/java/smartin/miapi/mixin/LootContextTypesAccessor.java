package smartin.miapi.mixin;

import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(LootContextTypes.class)
public interface LootContextTypesAccessor {
    @Invoker("register")
    static LootContextType register(String name, Consumer<LootContextType.Builder> type) {
        throw new AssertionError();
    }
}
