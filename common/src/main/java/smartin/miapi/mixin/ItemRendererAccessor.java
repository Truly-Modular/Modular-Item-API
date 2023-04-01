package smartin.miapi.mixin;

import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.render.item.ItemRenderer;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor {
    @Accessor("colors")
    ItemColors color();
}