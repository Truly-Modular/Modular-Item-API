package smartin.miapi.lootFunctions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.modules.properties.slot.SlotProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @header Module Swap Loot Function
 * @path /data_types/loot_functions/module_swap
 * @description_start The `ModuleSwapLootFunction` is a loot function that randomizes the modules of a modular item when generated as loot.
 * This function attempts to substitute modules with compatible alternatives.
 * The function also allows for the selection of a fallback material.
 * This loot function can be used to greatly diversify the loot found.
 * Its full ID is ```"miapi:module_swap"```
 * @description_end
 * @data material: An material ID to target a different material then found on the item. if none are set the most powerfull material on the item is chosen instead
 * @data chance: An optional (default = 1.0) that sets the chance for a swap to occur in the first place.
 * @data blacklist: An optional list of Module IDs representing modules that cannot be substituted.
 * @data whitelist: An optional list of Module IDs, if set blocks all entries not on this list.
 */

public record ModuleSwapLootFunction(
        ResourceLocation material,
        double chance,
        Optional<List<ResourceLocation>> blacklist,
        Optional<List<ResourceLocation>> whitelist
) implements LootItemFunction {

    public static MapCodec<ModuleSwapLootFunction> CODEC = RecordCodecBuilder.mapCodec((instance) ->
            instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("material", Miapi.id("empty"))
                            .forGetter(c -> c.material),
                    Codec.DOUBLE.fieldOf("chance").orElse(1.0)
                            .forGetter(c -> c.chance),
                    Codec.list(ResourceLocation.CODEC).optionalFieldOf("blacklist")
                            .forGetter(c -> c.blacklist),
                    Codec.list(ResourceLocation.CODEC).optionalFieldOf("whitelist")
                            .forGetter(c -> c.whitelist)
            ).apply(instance, ModuleSwapLootFunction::new));


    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return RegistryInventory.moduleSwapLootFunctionLootItemFunctionType;
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
                    if (otherMaterial != null && isHigher(highestMaterial, otherMaterial)) {
                        highestMaterial = otherMaterial;
                    }
                }
            }
            if (material != null) {
                Material fromJson = MaterialProperty.materials.get(material);
                if (highestMaterial == null || fromJson != null && isHigher(highestMaterial, fromJson)) {
                    highestMaterial = fromJson;
                }
            }
            root = randomizeModuleAndChildren(root, highestMaterial, lootContext.getRandom());
            root.writeToItem(modular);
            modular = ItemIdProperty.changeId(modular);
        }
        return modular;
    }

    ModuleInstance randomizeModuleAndChildren(ModuleInstance moduleInstance, Material fallBackMaterial, RandomSource randomSource) {
        if (randomSource.nextFloat() <= chance()) {
            moduleInstance = findPossibleSubstitute(moduleInstance, randomSource);
        }
        Map<String, ModuleInstance> submodules = new LinkedHashMap<>(moduleInstance.getSubModuleMap());
        for (var entry : submodules.entrySet()) {
            moduleInstance.setSubModule(entry.getKey(), randomizeModuleAndChildren(entry.getValue(), fallBackMaterial, randomSource));
            moduleInstance.clearCaches();
        }
        return moduleInstance;
    }

    ModuleInstance findPossibleSubstitute(ModuleInstance module, RandomSource randomSource) {
        // Collect all possible substitute modules
        List<ItemModule> possibleSubstitutes = RegistryInventory.modules.getFlatMap().values().stream()
                .filter(m -> {
                    if (whitelist().isPresent()) {
                        if (!whitelist().get().contains(m.id())) {
                            return false;
                        }
                    }
                    if (blacklist().isPresent()) {
                        if (blacklist().get().contains(m.id())) {
                            return false;
                        }
                    }
                    Map<String, SlotProperty.ModuleSlot> testSlots = SlotProperty.getSlots(module);
                    Map<String, SlotProperty.ModuleSlot> slots = new LinkedHashMap<>(SlotProperty.getInstance().getData(m).orElse(new LinkedHashMap<>()));
                    for (String key : testSlots.keySet()) {
                        if (!slots.containsKey(key)) {
                            return false; // Slot missing in candidate module
                        } else {
                            ModuleInstance subModule = module.getSubModule(key);
                            if (subModule != null && !slots.get(key).allowedIn(subModule)) {
                                return false; // Slot exists but subModule isn't allowed
                            }
                        }
                    }
                    SlotProperty.ModuleSlot parentSlot = SlotProperty.getSlotIn(module);
                    if (parentSlot != null && !parentSlot.allowedIn(m)) {
                        return false;
                    }
                    return true; // All slots are compatible
                })
                .toList();

        // If there are no substitutes, return the input module as default
        if (possibleSubstitutes.isEmpty()) {
            return module; // Return the input ModuleInstance as the substitute
        }

        // Select a random substitute from the list
        int randomIndex = randomSource.nextInt(possibleSubstitutes.size());
        ModuleInstance moduleInstance = new ModuleInstance(possibleSubstitutes.get(randomIndex));
        moduleInstance.moduleData = module.moduleData;
        moduleInstance.subModules = module.subModules;
        moduleInstance.clearCaches();
        return moduleInstance;
    }


    public boolean isHigher(Material material, Material other) {
        int roughMiningLevel = getTagSize(material.getIncorrectBlocksForDrops());
        int otherMiningLevel = getTagSize(other.getIncorrectBlocksForDrops());
        if (roughMiningLevel != otherMiningLevel) {
            return otherMiningLevel < roughMiningLevel;
        }
        double tierDiff = isHigher(material, other, "tier", false);
        if (tierDiff != 0.0) {
            return tierDiff > 0;
        }
        double hardness = isHigher(material, other, "hardness", true);
        if (hardness != 0.0) {
            return hardness > 0.5;
        }
        double flexibility = isHigher(material, other, "flexibility", true);
        if (flexibility != 0.0) {
            return flexibility > 1.0;
        }
        return false;
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
