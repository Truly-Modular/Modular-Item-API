package smartin.miapi.material.composite;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.material.JsonMaterial;
import smartin.miapi.material.Material;

import java.util.ArrayList;
import java.util.List;

import static smartin.miapi.material.MaterialProperty.materials;

public class CompositeMaterial extends JsonMaterial {
    public static ResourceLocation KEY = Miapi.id("composite_material");
    public Material parent;
    public Material finalMaterial;
    public double cost = 1.0;
    public static Codec<CompositeMaterial> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    Codec.DOUBLE
                            .optionalFieldOf("cost", 1.0)
                            .forGetter((material) -> material.cost),
                    ResourceLocation.CODEC
                            .fieldOf("parent")
                            .forGetter((material) -> material.parent.getID())
            ).apply(instance, (cost, materialKey) -> {
                Material material = materials.get(materialKey);
                List<Composite> composites = new ArrayList<>();
                Material buildMaterial = material;
                for (Composite composite : composites) {
                    buildMaterial = composite.composite(buildMaterial, Environment.isClient());
                }
                return new CompositeMaterial(material, buildMaterial, composites, cost, Environment.isClient());
            }));

    public static DataComponentType<CompositeMaterial> NBT_MATERIAL_COMPONENT = DataComponentType.<CompositeMaterial>builder()
            .persistent(CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC)).build();

    public CompositeMaterial(Material parent, Material buildMaterial, List<Composite> composites, double cost, boolean isClient) {
        super(KEY, parent.getDebugJson().deepCopy(), isClient);
        this.parent = parent;
        this.cost = cost;
        this.finalMaterial = finalMaterial;
    }

    @Override
    public ResourceLocation getID() {
        return KEY;
    }
}
