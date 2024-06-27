package smartin.miapi.mixin.client;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemProperties.class)
public interface ModelPredicateProviderRegistryAccessor {

    @Invoker("register")
    static void register(Item item, ResourceLocation id, ClampedItemPropertyFunction provider) {
        throw new AssertionError();
    }

}
