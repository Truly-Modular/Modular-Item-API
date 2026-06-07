package smartin.miapi.mixin.entity;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemCooldowns.class)
public interface ItemCooldownsAccessor {
    @Accessor("tickCount")
    int getMiapiTickCount();

    @Accessor("cooldowns")
    Map<Item, ItemCooldowns.CooldownInstance> getMiapiCooldowns();
}
