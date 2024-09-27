package smartin.miapi.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.jetbrains.annotations.NotNull;
import smartin.miapi.Miapi;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.material.AllowedMaterial;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaterialSwapLootFunction implements LootItemFunction {
    public ResourceLocation materialID;
    public double lowerBounds;
    public double upperBounds;
    public double miningLevelFactor;
    public double tierFactor;
    public double hardnessFactor;
    public double flexibilityFactor;

    public static MapCodec<MaterialSwapLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("material", Miapi.id("empty"))
                            .forGetter(c -> c.materialID),
                    Codec.DOUBLE.fieldOf("lower_bounds").orElse(-3.0)
                            .forGetter(c -> c.lowerBounds),
                    Codec.DOUBLE.fieldOf("upper_bounds").orElse(0.5)
                            .forGetter(c -> c.upperBounds),
                    Codec.DOUBLE.fieldOf("mining_level_factor").orElse(1.0)
                            .forGetter(c -> c.miningLevelFactor),
                    Codec.DOUBLE.fieldOf("tier_factor").orElse(1.0)
                            .forGetter(c -> c.tierFactor),
                    Codec.DOUBLE.fieldOf("hardness_factor").orElse(1.0)
                            .forGetter(c -> c.hardnessFactor),
                    Codec.DOUBLE.fieldOf("flexibility_factor").orElse(1.0)
                            .forGetter(c -> c.flexibilityFactor)
            ).apply(instance, MaterialSwapLootFunction::new));

    public MaterialSwapLootFunction(ResourceLocation material, double lowerBounds, double upperBounds, double miningLevelFactor,
                                    double tierFactor, double hardnessFactor, double flexibilityFactor) {
        this.materialID = material;
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
        this.miningLevelFactor = miningLevelFactor;
        this.tierFactor = tierFactor;
        this.hardnessFactor = hardnessFactor;
        this.flexibilityFactor = flexibilityFactor;
    }

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return RegistryInventory.materialSwapLootFunctionLootItemFunctionType;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        ItemStack modular = ModularItemStackConverter.getModularVersion(stack);
        if (ModularItem.isModularItem(modular)) {
            ModuleInstance root = ItemModule.getModules(modular);
            Material highestMaterial = MaterialProperty.getMaterial(root);
            for (ModuleInstance module : root.allSubModules()) {
                Material otherMaterial = MaterialProperty.getMaterial(module);
                if (highestMaterial == null) {
                    highestMaterial = otherMaterial;
                } else {
                    if (otherMaterial != null && isHigher(highestMaterial, otherMaterial) > 0) {
                        highestMaterial = otherMaterial;
                    }
                }
            }
            if (materialID != null) {
                Material fromJson = MaterialProperty.materials.get(materialID);
                if (highestMaterial == null || fromJson != null && isHigher(highestMaterial, fromJson) > 0) {
                    highestMaterial = fromJson;
                }
            }
            try {
                root = randomizeMaterialAndChildren(root, highestMaterial, lootContext.getRandom());
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("error during material swap function", e);
            }
            root.writeToItem(modular);
            modular = ItemIdProperty.changeId(modular);
        }
        return modular;
    }

    ModuleInstance randomizeMaterialAndChildren(ModuleInstance moduleInstance, Material fallBackMaterial, RandomSource randomSource) {
        moduleInstance = attemptRandomizeMaterial(moduleInstance, fallBackMaterial, randomSource);
        Map<String, ModuleInstance> submodules = new LinkedHashMap<>(moduleInstance.getSubModuleMap());
        for (var entry : submodules.entrySet()) {
            moduleInstance.setSubModule(entry.getKey(), randomizeMaterialAndChildren(entry.getValue(), fallBackMaterial, randomSource));
            moduleInstance.clearCaches();
        }
        return moduleInstance;
    }

    ModuleInstance attemptRandomizeMaterial(ModuleInstance module, Material fallBackMaterial, RandomSource randomSource) {
        Material currentMaterial = MaterialProperty.getMaterial(module);
        if (currentMaterial == null) {
            return module;
        }
        List<Material> possibleSubstitutes = MaterialProperty.materials.values().stream()
                .filter(m -> {
                    if (m.getID().toString().contains("custom")) {
                        return false;
                    }
                    if (AllowedMaterial.property.getData(module).isPresent() &&
                        !AllowedMaterial.property.getData(module).get().isValid(m)) {
                        return false;
                    }
                    double difToFallback = isHigher(fallBackMaterial, m);
                    if (!(difToFallback > lowerBounds && difToFallback < upperBounds)) {
                        return false;
                    }
                    return true;
                }).toList();
        int randomIndex = randomSource.nextInt(possibleSubstitutes.size() + 1);
        if (randomIndex == possibleSubstitutes.size()) {
            MaterialProperty.setMaterial(module, fallBackMaterial.getID());
        } else {
            Material m = possibleSubstitutes.get(randomIndex);
            MaterialProperty.setMaterial(module, m.getID());
        }
        return module;
    }


    public double isHigher(Material material, Material other) {
        double difference = 0;

        // Get the worst and best mining levels for comparison
        int worstMiningLevel = getTagSize(BlockTags.INCORRECT_FOR_WOODEN_TOOL);
        int bestMiningLevel = getTagSize(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

        // Calculate mining levels for the two materials
        int materialMiningLevel = getTagSize(material.getIncorrectBlocksForDrops());
        int otherMiningLevel = getTagSize(other.getIncorrectBlocksForDrops());

        // Calculate the percentage difference in mining level (relative to wood and netherite)
        if (materialMiningLevel != otherMiningLevel) {
            double miningLevelDiff = (double) (materialMiningLevel - otherMiningLevel) / (bestMiningLevel - worstMiningLevel);
            difference += miningLevelDiff * 10 * miningLevelFactor;
        }

        // Calculate tier difference, multiply by a factor of 2
        double tierDiff = isHigher(material, other, "tier", false);
        difference += tierDiff * 2 * tierFactor;

        // Calculate hardness difference
        double hardnessDiff = isHigher(material, other, "hardness", true);
        difference += hardnessDiff * hardnessFactor;

        // Calculate flexibility difference
        double flexibilityDiff = isHigher(material, other, "flexibility", true);
        difference += flexibilityDiff * 0.3 * flexibilityFactor;

        return -difference;
    }

    public double isHigher(Material material, Material other, String stat, boolean canBeZero) {
        double materialHardness = material.getDouble(stat);
        double otherMaterialHardness = other.getDouble(stat);
        if (!canBeZero) {
            if (materialHardness != otherMaterialHardness && materialHardness != 0 && otherMaterialHardness != 0) {
                return materialHardness - otherMaterialHardness;
            }
        } else {
            if (materialHardness != otherMaterialHardness) {
                return materialHardness - otherMaterialHardness;
            }
        }
        return 0.0;
    }

    public int getTagSize(TagKey<Block> tag) {
        return BuiltInRegistries.BLOCK.getTag(tag).map(holders -> (int) holders.stream().count()).orElse(0);
    }
}
