package smartin.miapi.material.generated;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.*;
import smartin.miapi.Miapi;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.material.base.Material;
import smartin.miapi.mixin.smithing.SmithingTransformRecipeAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static smartin.miapi.material.generated.GeneratedMaterialManager.verboseLogging;

/**
 * this class should be able to detect if a smithing recipe exists and then generate it,
 * also checking if the source material is a valid modular material in the process.
 */
public class SmithingRecipeUtil {
    public static RecipeManager manager = null;

    public static void setupSmithingRecipes(boolean isClient, RegistryAccess registryAccess, RecipeManager manager) {
        List<GeneratedMaterial> materials = MaterialProperty.MATERIAL_REGISTRY.getFlatMap() .values().stream().filter(GeneratedMaterial.class::isInstance).map(m -> (GeneratedMaterial) m).toList();
        materials.forEach(m -> MaterialProperty.MATERIAL_REGISTRY.remove(m.key));
        setupSmithingRecipe(materials, isClient, m -> MaterialProperty.MATERIAL_REGISTRY.register(m.key, m), registryAccess, null);
    }

    public static void setupSmithingRecipe(List<GeneratedMaterial> materials, boolean isClient, Consumer<GeneratedMaterial> register, RegistryAccess registryAccess, RecipeManager recipeManager) {
        try {
            if (recipeManager == null) {
                recipeManager = findManager(isClient);
            }
            if (registryAccess == null || recipeManager == null) {
                Miapi.LOGGER.warn("Could not setup Smithing Materials, could not find Recipes");
                materials.forEach(register);
                return;
            }
            List<GeneratedMaterial> todo = new ArrayList<>(materials);
            List<GeneratedMaterial> done = new ArrayList<>();
            AtomicBoolean hasMadeProgress = new AtomicBoolean(false);
            do {
                hasMadeProgress.set(false);
                List<GeneratedMaterial> currentTodo = new ArrayList<>(todo);
                for (GeneratedMaterial material : currentTodo) {
                    if (verboseLogging()) {
                        Miapi.LOGGER.info("testing material " + material.getStringID());
                    }
                    testForSmithingMaterial(recipeManager, registryAccess, material, (smithing) -> {
                        if (verboseLogging()) {
                            Miapi.LOGGER.info("registered smithing material " + smithing.getStringID());
                        }
                        todo.remove(smithing);
                        done.add(smithing);
                        register.accept(smithing);
                        hasMadeProgress.set(true);
                    }, (normal) -> {
                        if (verboseLogging()) {
                            Miapi.LOGGER.info("registered normal generated material " + normal.getStringID());
                        }
                        todo.remove(normal);
                        register.accept(normal);
                        hasMadeProgress.set(true);
                    });
                }
            } while (hasMadeProgress.get());
            todo.forEach(register);
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("Exception during SmithingRecipe setup!", e);
        }
    }

    public static boolean testForSmithingMaterial(
            RecipeManager manager, RegistryAccess
            registryManager,
            GeneratedMaterial material, Consumer<GeneratedMaterial> smithingMaterial, Consumer<GeneratedMaterial> normal) {
        try {
            var optionalRecipe = manager.getAllRecipesFor(RecipeType.SMITHING).stream()
                    .map(RecipeHolder::value)
                    .filter(SmithingTransformRecipe.class::isInstance)
                    //filter for only ItemChanging Recipes
                    .map(SmithingTransformRecipe.class::cast)
                    //check if the output is valid
                    .filter(recipe -> isValidRecipe(recipe, material.getSwordItem(), registryManager))
                    .findAny();
            if (optionalRecipe.isEmpty()) {
                normal.accept(material);
            }
            optionalRecipe.ifPresent(smithingTransformRecipe -> {
                ItemStack templateItem = Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe)
                                .getTemplate()
                                .getItems())
                        .filter(itemStack -> !itemStack.isEmpty())
                        .findAny()
                        .orElse(ItemStack.EMPTY);
                if (templateItem.isEmpty()) {
                    //is not a smithing material
                    normal.accept(material);
                    return;
                }
                var optional = Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe).getBase().getItems())
                        //making sure the input has a valid SourceMaterial
                        .filter(itemStack -> {
                            if (itemStack.getItem() instanceof TieredItem toolItem) {
                                Material parentMaterial = MaterialProperty.getMaterialFromIngredient(toolItem.getTier()
                                        .getRepairIngredient().getItems()[0]);
                                return parentMaterial != null;
                            }
                            return false;
                        })
                        .map(itemStack -> MaterialProperty.getMaterialFromIngredient(((TieredItem) itemStack.getItem()).getTier()
                                .getRepairIngredient().getItems()[0]))
                        .findAny();
                optional.ifPresent(sourceMaterial -> {
                    smithingMaterial.accept(material);
                    addSmithingRecipe(sourceMaterial, material, templateItem, smithingTransformRecipe, registryManager, manager);
                });
            });
        } catch (RuntimeException e) {
            Miapi.LOGGER.error("Error during Smithing recipe generation!", e);
        }
        return false;
    }

    static boolean isValidRecipe
            (SmithingTransformRecipe recipe, SwordItem swordItem, RegistryAccess manager) {
        if (recipe.getResultItem(manager).getItem().equals(swordItem)) {
            return true;
        }
        return ((SmithingTransformRecipeAccessor) recipe).getResult().getItem().equals(swordItem);
    }

    public static RecipeManager findManager(boolean isClient) {
        if (isClient) {
            if (Minecraft.getInstance().getConnection() != null) {
                var manager = Minecraft.getInstance().getConnection().getRecipeManager();
                if (manager != null) {
                    return manager;
                }
            }
            if (Miapi.server != null) {
                return Miapi.server.getRecipeManager();
            }
        } else {
            if (manager != null) {
                return manager;
            }
            if (Miapi.server != null) {
                return Miapi.server.getRecipeManager();
            }
        }
        return manager;
    }

    public static void addSmithingRecipe
            (Material sourceMaterial, GeneratedMaterial outputMaterial, ItemStack templateItem, SmithingTransformRecipe smithingTransformRecipe, RegistryAccess registryAccess, RecipeManager recipeManager) {
        Collection<RecipeHolder<?>> recipes = recipeManager.getRecipes();
        String id = "generated_material_recipe." + outputMaterial.getID() + "." + sourceMaterial.getID() + "." + BuiltInRegistries.ITEM.getKey(templateItem.getItem());
        id = id.replace(":", ".");
        ResourceLocation recipeId = Miapi.id(id);
        if (recipeManager.byKey(recipeId).isEmpty()) {
            Ingredient template = ((SmithingTransformRecipeAccessor) smithingTransformRecipe).getTemplate();
            MaterialSmithingRecipe materialSmithingRecipe = new MaterialSmithingRecipe(
                    template,
                    sourceMaterial.getID(),
                    ((SmithingTransformRecipeAccessor) smithingTransformRecipe).getAddition(),
                    outputMaterial.getID()
            );

            RecipeHolder<MaterialSmithingRecipe> materialSmithingRecipeRecipeHolder = new RecipeHolder<>(
                    recipeId, materialSmithingRecipe
            );
            recipes = new ArrayList<>(recipes);
            recipes.add(materialSmithingRecipeRecipeHolder);
            if (verboseLogging()) {
                Miapi.LOGGER.warn("added Smithing Recipe for " + sourceMaterial.getID() + " to " + outputMaterial.key + " via " + BuiltInRegistries.ITEM.getKey(templateItem.getItem()));
            }
            outputMaterial.setSmithingMaterial(sourceMaterial.getID(), template);
            recipeManager.replaceRecipes(recipes);
        }
    }
}
