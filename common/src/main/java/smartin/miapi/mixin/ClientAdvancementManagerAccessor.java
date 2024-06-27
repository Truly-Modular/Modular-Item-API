package smartin.miapi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;

@Mixin(ClientAdvancements.class)
public interface ClientAdvancementManagerAccessor {
    @Accessor
    Map<Advancement, AdvancementProgress> getAdvancementProgresses();
}
