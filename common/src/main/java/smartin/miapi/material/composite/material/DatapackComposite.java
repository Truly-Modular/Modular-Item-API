package smartin.miapi.material.composite.material;

import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.material.base.Material;
import smartin.miapi.material.composite.Composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DatapackComposite extends BasicOtherMaterialComposite {
    public static ResourceLocation ID = Miapi.id("data_composite");
    public static MapCodec<DatapackComposite> INNER_MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Miapi.ID_CODEC.fieldOf("data_composite").forGetter(m -> m.dataComposite)
            ).apply(instance, DatapackComposite::new));
    public static MapCodec<DatapackComposite> MAP_CODEC = CompositeFromOtherMaterial.getCodec(INNER_MAP_CODEC);
    public static final Map<ResourceLocation, List<Composite>> DATA_COMPOSITE_REGISTRY = new HashMap<>();
    public ResourceLocation dataComposite;
    public List<Composite> composites = List.of();

    static {
        Miapi.registerReloadHandler("miapi/data_composite", DATA_COMPOSITE_REGISTRY, Composite.CODEC.listOf(), 0.0f);
    }


    public DatapackComposite(ResourceLocation dataComposite) {
        super();
        this.dataComposite = dataComposite;
        if (!DATA_COMPOSITE_REGISTRY.containsKey(dataComposite)) {
            Miapi.LOGGER.error("Data Composite " + dataComposite + " NOT FOUND!");
        } else {
            composites = copy(DATA_COMPOSITE_REGISTRY.get(dataComposite));
        }
    }

    public List<Composite> copy(List<Composite> copies) {
        return Composite.CODEC.listOf().decode(JsonOps.INSTANCE, Composite.CODEC.listOf().encodeStart(JsonOps.INSTANCE, copies).getOrThrow()).getOrThrow().getFirst();
    }

    @Override
    public Material composite(Material parent, boolean isClient) {
        if (!DATA_COMPOSITE_REGISTRY.containsKey(dataComposite)) {
            Miapi.LOGGER.error("Data Composite " + dataComposite + " NOT FOUND!");
        }
        Material build = parent;
        for (Composite composite : composites) {
            if (composite instanceof CompositeFromOtherMaterial otherMaterial) {
                otherMaterial.setMaterial(material);
            }
            build = composite.composite(build, isClient);
        }
        return build;
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }
}
