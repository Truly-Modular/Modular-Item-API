package smartin.miapi.modules.material.generated;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import smartin.miapi.Miapi;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static smartin.miapi.modules.material.MaterialProperty.materials;

public class GeneratedMaterialManager {
    public static final List<GeneratedMaterial> generatedMaterials = new ArrayList<>();
    public static final List<GeneratedMaterialFromCopy> basicGeneratedMaterials = new ArrayList<>();

    public static void setup() {
        ReloadEvents.MAIN.subscribe(isClient -> {
            if (!isClient) {
                onReloadServer();
            }else{
                basicGeneratedMaterials.forEach(generatedMaterial -> materials.put(generatedMaterial.getKey(),generatedMaterial));
            }
        }, -1);
        ReloadEvents.dataSyncerRegistry.register("generated_materials", new ReloadEvents.SimpleSyncer<>(ByteBufCodecs.fromCodec(Codec.list(GeneratedMaterial.CODEC))) {

            @Override
            public List<GeneratedMaterial> getDataServer() {
                return generatedMaterials;
            }

            @Override
            public void interpretData(List<GeneratedMaterial> data) {
                generatedMaterials.clear();
                generatedMaterials.addAll(data);
                generatedMaterials.forEach(generatedMaterial -> materials.put(generatedMaterial.getKey(), generatedMaterial));
                SmithingRecipeUtil.setupSmithingRecipe(generatedMaterials);
            }
        });
        ReloadEvents.dataSyncerRegistry.register("generated_simple_materials",
                new ReloadEvents.SimpleSyncer<>(ByteBufCodecs.fromCodec(Codec.list(GeneratedMaterialFromCopy.CODEC))) {
            @Override
            public List<GeneratedMaterialFromCopy> getDataServer() {
                return basicGeneratedMaterials;
            }

            @Override
            public void interpretData(List<GeneratedMaterialFromCopy> data) {
                basicGeneratedMaterials.clear();
                basicGeneratedMaterials.addAll(data);
            }
        });
    }

    public static void onReloadServer() {
        try {
            if (verboseLogging()) {
                Miapi.LOGGER.info("MIAPI STARTED MATERIAL GENERATION");
            }
            generatedMaterials.clear();
            basicGeneratedMaterials.clear();
            if (!MiapiConfig.INSTANCE.server.generatedMaterials.generateMaterials) {
                return;
            }
            List<TieredItem> toolItems = BuiltInRegistries.ITEM.stream()
                    .filter(TieredItem.class::isInstance)
                    .map(TieredItem.class::cast)
                    .filter(toolMaterial ->
                            toolMaterial.getTier().getRepairIngredient() != null &&
                            toolMaterial.getTier().getRepairIngredient().getItems() != null &&
                            toolMaterial.getTier().getRepairIngredient().getItems().length > 0)
                    .filter(toolMaterial -> !toolMaterial.getTier().getRepairIngredient().getItems()[0].is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                    .filter(toolMaterial -> Arrays.stream(toolMaterial.getTier().getRepairIngredient().getItems())
                            .allMatch(itemStack -> MaterialProperty.getMaterialFromIngredient(itemStack) == null && !itemStack.getItem().equals(Items.BARRIER)))
                    .toList();
            if (MiapiConfig.INSTANCE.server.generatedMaterials.generateOtherMaterials) {
                toolItems.stream()
                        .map(TieredItem::getTier)
                        .collect(Collectors.toSet())
                        .stream()
                        .limit(MiapiConfig.INSTANCE.server.generatedMaterials.maximumGeneratedMaterials)
                        .collect(Collectors.toSet()).forEach(toolMaterial -> {
                            try {
                                ItemStack mainIngredient = toolMaterial.getRepairIngredient().getItems()[0];
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
                                            Miapi.LOGGER.info("Generated Material " + generatedMaterial.getKey());
                                        }
                                        generatedMaterials.add(generatedMaterial);
                                    }
                                }
                            } catch (Exception e) {
                                Miapi.LOGGER.error("could not generate Material for " + toolMaterial.getRepairIngredient().getItems()[0], e);
                            }
                        });
            }

            generatedMaterials.forEach(material -> {
                materials.put(material.getKey(), material);
            });
            SmithingRecipeUtil.setupSmithingRecipe(generatedMaterials);

            if (MiapiConfig.INSTANCE.server.generatedMaterials.generateWoodMaterials) {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.getDefaultInstance().is(ItemTags.PLANKS) &&
                                        !item.getDefaultInstance().is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                        .limit(MiapiConfig.INSTANCE.server.generatedMaterials.maximumGeneratedMaterials)
                        .forEach(item -> {
                            try {
                                if (isValidItem(item)) {
                                    Material old = MaterialProperty.getMaterialFromIngredient(item.getDefaultInstance());
                                    if (old != null && old == materials.get("wood")) {
                                        GeneratedMaterialFromCopy generatedMaterial = new GeneratedMaterialFromCopy(
                                                item.getDefaultInstance(),
                                                old
                                        );
                                        basicGeneratedMaterials.add(generatedMaterial);
                                        if (verboseLogging()) {
                                            Miapi.LOGGER.info("Generated Wood Material " + generatedMaterial.getKey());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Miapi.LOGGER.error("could not generate wood Material for " + item.getDescriptionId(), e);
                            }
                        });
            }

            if (MiapiConfig.INSTANCE.server.generatedMaterials.generateStoneMaterials) {
                BuiltInRegistries.ITEM.stream()
                        .filter(item -> item.getDefaultInstance().is(ItemTags.STONE_TOOL_MATERIALS) &&
                                        !item.getDefaultInstance().is(RegistryInventory.MIAPI_FORBIDDEN_TAG))
                        .limit(MiapiConfig.INSTANCE.server.generatedMaterials.maximumGeneratedMaterials)
                        .forEach(item -> {
                            try {
                                if (isValidItem(item) && !item.equals(Items.COBBLESTONE)) {
                                    Material old = MaterialProperty.getMaterialFromIngredient(item.getDefaultInstance());
                                    if (old != null && old == materials.get("stone")) {
                                        GeneratedMaterialFromCopy generatedMaterial = new GeneratedMaterialFromCopy(
                                                item.getDefaultInstance(),
                                                old
                                        );
                                        basicGeneratedMaterials.add(generatedMaterial);
                                        if (verboseLogging()) {
                                            Miapi.LOGGER.info("Generated Stone Material " + generatedMaterial.getKey());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Miapi.LOGGER.error("could not generate stone Material for " + item.getDescriptionId(), e);
                            }
                        });
            }
            for (Material material : basicGeneratedMaterials) {
                materials.put(material.getKey(), material);
            }
            if (verboseLogging()) {
                Miapi.LOGGER.info("MIAPI FINISHED MATERIAL GENERATION");
            }
        } catch (Exception e) {
            Miapi.LOGGER.error("MAJOR ISSUE DURING MATERIAL CREATION", e);
        }
    }

    public static boolean isValidItem(Item item) {
        ResourceLocation identifier = BuiltInRegistries.ITEM.getKey(item);
        Pattern pattern = Pattern.compile(MiapiConfig.INSTANCE.server.generatedMaterials.blockRegex);
        return !pattern.matcher(identifier.toString()).find();
    }


    public static boolean verboseLogging() {
        return MiapiConfig.INSTANCE.server.other.verboseLogging;
    }
}
