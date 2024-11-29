package smartin.miapi.material.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.Material;

import java.util.HashMap;
import java.util.Map;

public interface Composite {
    Map<ResourceLocation, MapCodec<Composite>> COMPOSITE_REGISTRY = new HashMap<>();
    Codec<Composite> CODEC = Miapi.ID_CODEC.dispatch(Composite::getID, COMPOSITE_REGISTRY::get);

    Material composite(Material parent, boolean isClient);

    ResourceLocation getID();
}
