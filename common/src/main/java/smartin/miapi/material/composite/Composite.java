package smartin.miapi.material.composite;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.material.Material;

import java.util.HashMap;
import java.util.Map;

public interface Composite {
    Map<ResourceLocation, Codec<Composite>> COMPOSITE_REGISTRY = new HashMap<>();

    Material composite(Material parent, boolean isClient);

    ResourceLocation getID();
}
