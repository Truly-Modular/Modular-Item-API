package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.List;

public class InteractFromInventoryFeature implements InventoryFeatureType<List<ResourceLocation>> {
    public static final InteractFromInventoryFeature FEATURE = new InteractFromInventoryFeature();

    private InteractFromInventoryFeature() {
    }

    @Override
    public ResourceLocation id() {
        return Miapi.id("interact_inventory_item_binding");
    }

    @Override
    public Codec<List<ResourceLocation>> codec() {
        return Miapi.toListOrSimple(Miapi.ID_CODEC);
    }

    @Override
    public List<ResourceLocation> initialize(List<ResourceLocation> property, ModuleInstance context) {
        return property;
    }

    @Override
    public List<ResourceLocation> merge(List<ResourceLocation> left, List<ResourceLocation> right, MergeType mergeType) {
        return MergeAble.mergeList(left, right, mergeType);
    }
}