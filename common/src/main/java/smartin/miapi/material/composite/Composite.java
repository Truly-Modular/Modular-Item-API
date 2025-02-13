package smartin.miapi.material.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Composite {
    Map<ResourceLocation, MapCodec<? extends Composite>> COMPOSITE_REGISTRY = new HashMap<>();
    //Miapi.LOGGER.info("encoding composite " + c.getID());
    Codec<Composite> CODEC = Miapi.ID_CODEC.dispatch(Composite::getID, (id) -> COMPOSITE_REGISTRY.getOrDefault(id, AnyIngredientComposite.MAP_CODEC));

    Material composite(Material parent, boolean isClient);

    default Optional<Component> getDescription() {
        return Optional.empty();
    }

    ResourceLocation getID();
}
