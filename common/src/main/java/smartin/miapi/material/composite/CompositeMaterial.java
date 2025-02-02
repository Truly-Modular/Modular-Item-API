package smartin.miapi.material.composite;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.event.EventResult;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.material.*;
import smartin.miapi.material.base.Material;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static smartin.miapi.material.MaterialProperty.materials;

public class CompositeMaterial extends DelegatingMaterial {
    public static ResourceLocation KEY = Miapi.id("composite_material");
    public List<Composite> compositeList;
    public double cost = 1.0;
    public static MapCodec<CompositeMaterial> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    Composite.CODEC
                            .listOf()
                            .optionalFieldOf("composites", List.of())
                            .forGetter((material) -> material.compositeList)
            ).apply(instance, CompositeMaterial::getFromComposites));

    static {
        ReloadEvents.MAIN.subscribe((isClient, registryAccess) -> {
            JsonObject object = new JsonObject();
            materials.put(
                    KEY,
                    new CompositeMaterial(new JsonMaterial(KEY, object, isClient), List.of()));
        }, -1);
        MiapiEvents.MATERIAL_CRAFT_EVENT.register(data -> {
            if (data.material instanceof CompositeMaterial componentMaterial) {
                //componentMaterial.writeMaterial(data.moduleInstance);
                //data.moduleInstance.getRoot().writeToItem(data.crafted);
            }
            return EventResult.pass();
        });
        Composite.COMPOSITE_REGISTRY.put(MaterialCopyComposite.ID, MaterialCopyComposite.MAP_CODEC);
        Composite.COMPOSITE_REGISTRY.put(AnyIngredientComposite.ID, AnyIngredientComposite.MAP_CODEC);
    }

    public static DataComponentType<CompositeMaterial> COMPOSITE_MATERIAL_COMPONENT = DataComponentType.<CompositeMaterial>builder()
            .persistent(CODEC.codec())
            .networkSynchronized(ByteBufCodecs.fromCodec(CODEC.codec())).build();

    protected CompositeMaterial(Material buildMaterial, List<Composite> composites) {
        super(buildMaterial);
        this.compositeList = composites;
    }

    public static CompositeMaterial getFromComposites(List<Composite> composites) {
        Material buildMaterial = new DefaultMaterial();
        for (Composite composite : composites) {
            buildMaterial = composite.composite(buildMaterial, Environment.isClient());
        }
        return new CompositeMaterial(buildMaterial, composites);
    }

    @Override
    public Material getMaterialFromIngredient(ItemStack ingredient) {
        return ingredient.getOrDefault(COMPOSITE_MATERIAL_COMPONENT, null);
    }

    public Double getPriorityOfIngredientItem(ItemStack ingredient) {
        CompositeMaterial material = ingredient.get(COMPOSITE_MATERIAL_COMPONENT);
        if (material != null) {
            return material.parent.getPriorityOfIngredientItem(ingredient);
        }
        return null;
    }

    @Override
    public ResourceLocation getID() {
        return KEY;
    }

    @Override
    public Optional<MapCodec<? extends Material>> codec() {
        return Optional.of(CODEC);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Check for reference equality
        if (o == null || getClass() != o.getClass()) return false; // Check for null or class mismatch
        CompositeMaterial that = (CompositeMaterial) o; // Cast to CompositeMaterial
        return Double.compare(that.cost, cost) == 0 && // Compare cost (double) using Double.compare
               Objects.equals(compositeList, that.compositeList) && // Compare composites list
               Objects.equals(KEY, that.KEY); // Compare KEY (ResourceLocation)
    }

    @Override
    public int hashCode() {
        return Objects.hash(KEY, compositeList, cost); // Compute hash using KEY, composites, and cost
    }

}
