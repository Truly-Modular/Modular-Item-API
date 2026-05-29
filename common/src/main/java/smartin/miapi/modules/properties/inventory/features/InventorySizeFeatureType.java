package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeType;

public class InventorySizeFeatureType implements InventoryFeatureType<DoubleOperationResolvable> {

    public static final InventorySizeFeatureType FEATURE = new InventorySizeFeatureType();

    private InventorySizeFeatureType() {

    }

    public static final ResourceLocation ID =
            Miapi.id("size");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<DoubleOperationResolvable> codec() {
        return DoubleOperationResolvable.CODEC;
    }

    @Override
    public DoubleOperationResolvable initialize(DoubleOperationResolvable property, ModuleInstance context) {
        return property.initialize(context);
    }

    @Override
    public DoubleOperationResolvable merge(DoubleOperationResolvable left, DoubleOperationResolvable right, MergeType mergeType) {
        return DoubleOperationResolvable.merge(left, right, mergeType);
    }
}