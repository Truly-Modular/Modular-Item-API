package smartin.miapi.lootFunctions;

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
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @header Material Swap Loot Function
 * @path /data_types/loot_functions/material_swap
 * @description_start The `MaterialSwapLootFunction` is a loot function
 * that randomizes the materials of a modular item when generated
 * as loot. This function compares the item’s materials with a specified material
 * and replaces it with a new one based on several configurable parameters.
 * These parameters control the bounds, mining level, tier, hardness, and flexibility
 * factors that influence the selection of substitute materials.
 * Additionally, the function can filter materials using
 * a whitelist or blacklist to limit or exclude certain materials from substitution.
 * This allows for very random items within the loot-pool that still retain roughly the same strength level
 * * Its full ID is ```"miapi:material_swap"```
 * @description_end
 * @data material: An optional material ID to substitute, if not set the highest found material will be used instead
 * @data lowerBounds: An optional (default = -3) double that sets the lower bound of acceptable material differences for substitution.
 * @data upperBounds: An optional (default = +0.5) double that sets the upper bound of acceptable material differences for substitution.
 * @data miningLevelFactor: An optional (default = 1.0) double that adjusts the impact of the mining level on material substitution.
 * @data tierFactor: An optional (default = 1.0) double that influences the selection of materials based on their tier level.
 * @data hardnessFactor: An optional double (default = 1.0) that influences the selection of materials based on their hardness.
 * @data flexibilityFactor: An optional (default = 1.0) double that adjusts material substitution based on flexibility values.
 * @data chance: An optional (default = 1.0) that sets the chance for a swap to occur in the first place.
 * @data blacklist: An optional list of Material IDs representing materials that cannot be substituted.
 * @data whitelist: An optional list of Material IDs, if set blocks all entries not on this list.
 */

public record MaterialSwapLootFunction(
        ResourceLocation material,
        double lowerBounds,
        double upperBounds,
        double miningLevelFactor,
        double tierFactor,
        double hardnessFactor,
        double flexibilityFactor,
        double chance,
        Optional<List<ResourceLocation>> blacklist,
        Optional<List<ResourceLocation>> whitelist

) implements LootItemFunction {

    public static MapCodec<MaterialSwapLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("material", Miapi.id("empty"))
                            .forGetter(c -> c.material),
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
                            .forGetter(c -> c.flexibilityFactor),
                    Codec.DOUBLE.fieldOf("chance").orElse(1.0)
                            .forGetter(c -> c.chance),
                    Codec.list(ResourceLocation.CODEC).optionalFieldOf("blacklist")
                            .forGetter(c -> c.blacklist),
                    Codec.list(ResourceLocation.CODEC).optionalFieldOf("whitelist")
                            .forGetter(c -> c.whitelist)
            ).apply(instance, MaterialSwapLootFunction::new));

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return RegistryInventory.materialSwapLootFunctionLootItemFunctionType;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        ItemStack modular = ModularItemStackConverter.getModularVersion(stack);
        if (ModularItem.isModularItem(modular)) {
            try {
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
                if (material != null) {
                    Material fromJson = MaterialProperty.materials.get(material);
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
            } catch (RuntimeException e) {
                Miapi.LOGGER.error("Issue during Material Swap", e);
            }
        }
        return modular;
    }

    ModuleInstance randomizeMaterialAndChildren(ModuleInstance moduleInstance, Material fallBackMaterial, RandomSource randomSource) {
        if (randomSource.nextFloat() <= chance()) {
            try {
                moduleInstance = attemptRandomizeMaterial(moduleInstance, fallBackMaterial, randomSource);
            } catch (RuntimeException runtimeException) {
                Miapi.LOGGER.error("Issue during Material Swap", runtimeException);
            }
        }
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
        if (AllowedMaterial.property.getData(module).isPresent() &&
            !AllowedMaterial.property.getData(module).get().isValid(fallBackMaterial)) {
            fallBackMaterial = currentMaterial;
        }
        Material finalFallBackMaterial = fallBackMaterial;
        List<Material> possibleSubstitutes = MaterialProperty.materials.values().stream()
                .filter(m -> {
                    if (m.getID().toString().contains("custom")) {
                        return false;
                    }
                    if (whitelist().isPresent()) {
                        if (!whitelist().get().contains(m.getID())) {
                            return false;
                        }
                    }
                    if (blacklist().isPresent()) {
                        if (blacklist().get().contains(m.getID())) {
                            return false;
                        }
                    }
                    if (AllowedMaterial.property.getData(module).isPresent() &&
                        !AllowedMaterial.property.getData(module).get().isValid(m)) {
                        return false;
                    }
                    double difToFallback = isHigher(finalFallBackMaterial, m);
                    if (!(difToFallback > lowerBounds && difToFallback < upperBounds)) {
                        return false;
                    }
                    return true;
                }).toList();
        int randomIndex = randomSource.nextInt(possibleSubstitutes.size() + 1);
        if (randomIndex == possibleSubstitutes.size()) {
            MaterialProperty.setMaterial(module, fallBackMaterial);
        } else {
            Material m = possibleSubstitutes.get(randomIndex);
            MaterialProperty.setMaterial(module, m);
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
        difference += tierDiff * 0.5 * tierFactor;

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

    public static int getTagSize(TagKey<Block> tag) {
        return BuiltInRegistries.BLOCK.getTag(tag).map(holders -> (int) holders.stream().count()).orElse(0);
    }
}
