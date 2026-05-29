package smartin.miapi.modules.properties.inventory;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.inventory.features.InventoryFeatureType;

import java.util.Map;

public class InventoryType {
    public static Codec<InventoryType> CODEC = AutoCodec.of(InventoryType.class).codec();
    @CodecBehavior.Optional
    public Component name = Component.literal("missing name " + getId());

    @CodecBehavior.Optional
    public double priority = 0.0;

    @AutoCodec.Ignored
    public ResourceLocation id;

    @CodecBehavior.Optional
    public InventoryFeatureType.FeatureSet features = new InventoryFeatureType.FeatureSet(Map.of());

    public ResourceLocation getId() {
        return id;
    }

    public Component getName() {
        return name;
    }

    public double priority() {
        return priority;
    }

    InventoryFeatureType.FeatureSet getDefaultFeatures() {
        return features;
    }

    public InventoryType additionalSetup(ResourceLocation id, RegistryAccess registryAccess) {
        this.features = features.initialize(features, new ModuleInstance(ItemModule.empty, registryAccess));
        this.id = id;
        return this;
    }
}
