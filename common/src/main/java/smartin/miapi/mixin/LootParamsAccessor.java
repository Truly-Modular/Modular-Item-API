package smartin.miapi.mixin;

import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(LootParams.class)
public interface LootParamsAccessor {
    @Accessor
    Map<LootContextParam<?>, Object> getParams();
}
