package smartin.miapi.material.generated;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static smartin.miapi.material.MaterialProperty.MATERIAL_REGISTRY;

public class GeneratedMaterialManager {
    public static final List<GeneratedMaterial> generatedMaterials = new ArrayList<>();
    public static final List<GeneratedMaterialFromCopy> basicGeneratedMaterials = new ArrayList<>();

    public static void setup() {
        ReloadEvents.MAIN.subscribe((isClient, registryAccess) -> {
            if (!isClient) {
                onReloadServer(registryAccess);
            } else {
                basicGeneratedMaterials.forEach(generatedMaterial -> MATERIAL_REGISTRY.register(generatedMaterial.getID(), generatedMaterial));
                SmithingRecipeUtil.setupSmithingRecipe(generatedMaterials, true, (material) -> {
                    MATERIAL_REGISTRY.register(material.getID(), material);
                }, registryAccess, null);
            }
        }, -1);
        ReloadEvents.dataSyncerRegistry.register(Miapi.id("generated_materials"),
                new ReloadEvents.SimpleSyncer<>(ByteBufCodecs.fromCodec(Codec.list(GeneratedMaterial.CODEC))) {
                    @Override
                    public List<GeneratedMaterial> getDataServer() {
                        return generatedMaterials;
                    }

                    @Override
                    public void interpretData(List<GeneratedMaterial> data) {
                        if (verboseLogging()) {
                            Miapi.LOGGER.info("Client received " + data.size() + " complex materials");
                        }
                        for (GeneratedMaterial material : data) {
                            material.setupClient();
                            if (verboseLogging()) {
                                Miapi.LOGGER.info("complex " + material.getTranslation().getString());
                            }
                        }
                        generatedMaterials.clear();
                        generatedMaterials.addAll(data);
                    }
                });
        ReloadEvents.dataSyncerRegistry.register(Miapi.id("generated_simple_materials"),
                new ReloadEvents.SimpleSyncer<>(ByteBufCodecs.fromCodec(Codec.list(GeneratedMaterialFromCopy.CODEC))) {
                    @Override
                    public List<GeneratedMaterialFromCopy> getDataServer() {
                        return basicGeneratedMaterials;
                    }

                    @Override
                    public void interpretData(List<GeneratedMaterialFromCopy> data) {
                        if (verboseLogging()) {
                            Miapi.LOGGER.info("Client received " + data.size() + " wood/stone materials");
                        }
                        for (GeneratedMaterialFromCopy copy : data) {
                            copy.setupClient();
                            if (verboseLogging()) {
                                Miapi.LOGGER.info("wood/stone " + copy.getTranslation().getString());
                            }
                        }
                        basicGeneratedMaterials.clear();

                        basicGeneratedMaterials.addAll(data);
                    }
                });
    }

    public static Registry<Item> getRegistry() {
        if (Miapi.registryAccess == null) {
            TagManager m;
            return BuiltInRegistries.ITEM;
        }
        return Miapi.registryAccess.registry(Registries.ITEM).get();
    }

    public static void onReloadServer(RegistryAccess access) {
        try {
            if (verboseLogging()) {
                Miapi.LOGGER.info("MIAPI STARTED MATERIAL GENERATION");
            }
            generatedMaterials.clear();
            basicGeneratedMaterials.clear();
            if (!MiapiConfig.getServerConfig().generatedMaterials.generateMaterials) {
                return;
            }
            var registiry = getRegistry();
            List<TieredItem> toolItems = new ArrayList<>(registiry.stream()
                    .filter(TieredItem.class::isInstance)
                    .map(TieredItem.class::cast)
                    .filter(toolMaterial ->
                            toolMaterial.getTier().getRepairIngredient() != null &&
                            toolMaterial.getTier().getRepairIngredient().getItems() != null &&
                            toolMaterial.getTier().getRepairIngredient().getItems().length > 0)
                    .filter(toolMaterial -> !toolMaterial.getTier().getRepairIngredient().getItems()[0].is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                    .filter(GeneratedMaterialManager::isValidItem)
                    .filter(toolMaterial -> Arrays.stream(toolMaterial.getTier().getRepairIngredient().getItems())
                            .allMatch(itemStack -> MaterialProperty.getMaterialFromIngredient(itemStack) == null && !itemStack.getItem().equals(Items.BARRIER)))

                    .toList());
            Map<Tier, List<TieredItem>> tieredItem = new HashMap<>();
            toolItems.forEach(item -> {
                tieredItem.computeIfAbsent(item.getTier(), (i) -> new ArrayList<>()).add(item);
            });
            Map<Tier, List<TieredItem>> consolidated = new HashMap<>();

            for (Map.Entry<Tier, List<TieredItem>> entry : tieredItem.entrySet()) {
                Tier currentTier = entry.getKey();
                List<TieredItem> currentItems = entry.getValue();

                boolean merged = false;

                for (Map.Entry<Tier, List<TieredItem>> consolidatedEntry : consolidated.entrySet()) {
                    Tier existingTier = consolidatedEntry.getKey();

                    if (isSameTier(currentTier, existingTier)) {
                        consolidatedEntry.getValue().addAll(currentItems);
                        merged = true;
                        break;
                    }
                }

                if (!merged) {
                    // No matching tier found, so just put a fresh entry
                    consolidated.put(currentTier, new ArrayList<>(currentItems));
                }
            }
            Map<Tier, List<TieredItem>> insufficientItems = new HashMap<>();
            consolidated.forEach((t, items) -> {
                boolean hasSword = items.stream().anyMatch(SwordItem.class::isInstance);
                boolean hasAxe = items.stream().anyMatch(AxeItem.class::isInstance);
                if (!(hasSword && hasAxe)) {
                    insufficientItems.put(t, items);
                }
            });
            insufficientItems.forEach((t, items) -> consolidated.remove(t));

            if (MiapiConfig.getServerConfig().generatedMaterials.generateOtherMaterials) {
                consolidated.forEach((tier, tieredItems) -> {
                    ItemStack mainIngredient = tier.getRepairIngredient().getItems()[0];
                    if (verboseLogging()) {
                        Miapi.LOGGER.info("attempting material generation for " + mainIngredient.getHoverName().getString());
                    }
                    if (isValidItem(mainIngredient.getItem())) {
                        GeneratedMaterial generatedMaterial = new GeneratedMaterial(
                                mainIngredient,
                                tier.getRepairIngredient(),
                                tier,
                                tieredItems
                        );
                        if (generatedMaterial.isValid()) {
                            if (verboseLogging()) {
                                Miapi.LOGGER.info("Generated Material " + generatedMaterial.getID());
                            }
                            generatedMaterials.add(generatedMaterial);
                        }
                    }
                });
                /*
                toolItems.stream()
                        .map(TieredItem::getTier)
                        .collect(Collectors.toSet())
                        .stream()
                        .limit(MiapiConfig.getServerConfig().generatedMaterials.maximumGeneratedMaterials)
                        .collect(Collectors.toSet()).forEach(toolMaterial -> {
                            try {
                                ItemStack mainIngredient = toolMaterial.getRepairIngredient().getItems()[0];
                                if (verboseLogging()) {
                                    Miapi.LOGGER.info("attempting material generation for " + mainIngredient.getHoverName().getString());
                                }
                                if (isValidItem(mainIngredient.getItem())) {
                                    List<TieredItem> tieredItems = toolItems.stream().filter(item -> toolMaterial.equals(item.getTier())).toList();
                                    GeneratedMaterial generatedMaterial = new GeneratedMaterial(
                                            mainIngredient,
                                            toolMaterial.getRepairIngredient(),
                                            toolMaterial,
                                            tieredItems
                                    );
                                    if (generatedMaterial.isValid()) {
                                        if (verboseLogging()) {
                                            Miapi.LOGGER.info("Generated Material " + generatedMaterial.getID());
                                        }
                                        generatedMaterials.add(generatedMaterial);
                                    }
                                }
                            } catch (RuntimeException e) {
                                Miapi.LOGGER.error("could not generate Material for " + toolMaterial.getRepairIngredient().getItems()[0], e);
                            }
                        });

                 */
            }


            if (MiapiConfig.getServerConfig().generatedMaterials.generateWoodMaterials) {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.getDefaultInstance().is(ItemTags.PLANKS) &&
                                        !item.getDefaultInstance().is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                        .limit(MiapiConfig.getServerConfig().generatedMaterials.maximumGeneratedMaterials)
                        .forEach(item -> {
                            try {
                                if (isValidItem(item)) {
                                    Material old = MaterialProperty.getMaterialFromIngredient(item.getDefaultInstance());
                                    Material baseWood = MATERIAL_REGISTRY.get(Miapi.id("wood/wood"));
                                    if (old != null && baseWood != null && old == baseWood) {
                                        GeneratedMaterialFromCopy generatedMaterial = new GeneratedMaterialFromCopy(
                                                item.getDefaultInstance(),
                                                old
                                        );
                                        basicGeneratedMaterials.add(generatedMaterial);
                                        if (verboseLogging()) {
                                            Miapi.LOGGER.info("Generated Wood Material " + generatedMaterial.getID());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Miapi.LOGGER.error("could not generate wood Material for " + item.getDescriptionId(), e);
                            }
                        });
            }

            if (MiapiConfig.getServerConfig().generatedMaterials.generateStoneMaterials) {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.getDefaultInstance().is(ItemTags.STONE_TOOL_MATERIALS) &&
                                        !item.getDefaultInstance().is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                        .limit(MiapiConfig.getServerConfig().generatedMaterials.maximumGeneratedMaterials)
                        .forEach(item -> {
                            try {
                                if (isValidItem(item) && !item.equals(Items.COBBLESTONE)) {
                                    Material old = MaterialProperty.getMaterialFromIngredient(item.getDefaultInstance());
                                    Material baseStone = MATERIAL_REGISTRY.get(Miapi.id("stone/stone"));
                                    if (old != null && baseStone != null && old == baseStone) {
                                        GeneratedMaterialFromCopy generatedMaterial = new GeneratedMaterialFromCopy(
                                                item.getDefaultInstance(),
                                                old
                                        );
                                        basicGeneratedMaterials.add(generatedMaterial);
                                        if (verboseLogging()) {
                                            Miapi.LOGGER.info("Generated Stone Material " + generatedMaterial.getID());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Miapi.LOGGER.error("could not generate stone Material for " + item.getDescriptionId(), e);
                            }
                        });
            }
            SmithingRecipeUtil.setupSmithingRecipe(generatedMaterials, false, (material -> {
                MATERIAL_REGISTRY.register(material.getID(), material);
                MiapiEvents.GENERATE_MATERIAL_CONVERTERS.invoker().generated(material, material.toolItems, material.armorItems, smartin.miapi.Environment.isClient());
            }), access, null);
            if (verboseLogging()) {
                Miapi.LOGGER.info("MIAPI FINISHED MATERIAL GENERATION");
            }
        } catch (Exception e) {
            Miapi.LOGGER.error("MAJOR ISSUE DURING MATERIAL CREATION", e);
        }
    }

    public static boolean isSameTier(Tier first, Tier second) {
        if (first.equals(second)) {
            return true;
        }
        try {
            if (!first.getIncorrectBlocksForDrops().equals(second.getIncorrectBlocksForDrops())) {
                return false;
            }
        } catch (RuntimeException runtimeException) {
            return false;
        }
        try {
            if (!isSameIngredient(first.getRepairIngredient(), second.getRepairIngredient())) {
                return false;
            }
            if (percentDif(first.getAttackDamageBonus(), second.getAttackDamageBonus()) > 0.1) {
                return false;
            }
            if (percentDif(first.getSpeed(), second.getSpeed()) > 0.1) {
                return false;
            }
            if (percentDif(first.getUses(), second.getUses()) >0.1) {
                return false;
            }
            if (percentDif(first.getEnchantmentValue(), second.getEnchantmentValue()) > 0.1) {
                return false;
            }
        } catch (RuntimeException runtimeException) {
            return false;
        }
        return true;
    }

    public static double percentDif(int a, int b) {
        return percentDif(a, (double) b);
    }

    public static double percentDif(float a, float b) {
        return percentDif(a, (double) b);
    }

    public static double percentDif(double a, double b) {
        return (Math.abs(a - b) / ((a + b) / 2.0)) * 100.0;
    }

    public static Tier selectBetterTier(Tier first, Tier second) {
        if (first.getAttackDamageBonus() == second.getAttackDamageBonus()) {
            return first.getSpeed() > second.getSpeed() ? first : second;
        }
        if (first.getAttackDamageBonus() > second.getAttackDamageBonus()) {
            return first;
        }
        return second;
    }

    public static boolean isSameIngredient(Ingredient first, Ingredient second) {
        var secondItems = Arrays.stream(second.getItems()).collect(Collectors.toSet());
        var firstItems = Arrays.stream(first.getItems()).collect(Collectors.toSet());
        if (secondItems.size() == firstItems.size()) {
            return firstItems.containsAll(secondItems);
        }
        return false;
    }

    public static boolean isValidItem(Item item) {
        ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);
        Pattern pattern = Pattern.compile(MiapiConfig.getServerConfig().generatedMaterials.blockRegex);
        return !pattern.matcher(identifier.toString()).find() &&
               !item.builtInRegistryHolder().is(RegistryInventory.MIAPI_FORBIDDEN_TAG);
    }


    public static boolean verboseLogging() {
        return MiapiConfig.getServerConfig().other.verboseLogging;
    }
}
