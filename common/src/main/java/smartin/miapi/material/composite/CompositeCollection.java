package smartin.miapi.material.composite;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.registries.MiapiRegistry;

import java.util.ArrayList;
import java.util.List;

public class CompositeCollection implements Composite {
    public static MiapiRegistry<CompositeCollection> REGISTRY = MiapiRegistry.getInstance(CompositeCollection.class);
    public static ResourceLocation ID = Miapi.id("composite_collection");
    public List<Composite> composites = new ArrayList<>();

    @Override
    public Material composite(Material parent, boolean isClient) {
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
