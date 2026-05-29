package smartin.miapi.modules.properties.inventory.features;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class AutoPickupFeatureType implements InventoryFeatureType<Boolean> {
    public static final AutoPickupFeatureType FEATURE = new AutoPickupFeatureType();

    private AutoPickupFeatureType() {

    }

    public static final ResourceLocation ID =
            Miapi.id("auto_pickup");

    @Override
    public Boolean merge(Boolean left, Boolean right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Codec<Boolean> codec() {
        return Codec.BOOL;
    }

    @Override
    public Boolean initialize(Boolean property, ModuleInstance context) {
        return property;
    }
}