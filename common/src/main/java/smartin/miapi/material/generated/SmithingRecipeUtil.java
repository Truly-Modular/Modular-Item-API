package smartin.miapi.material.generated;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.item.MaterialSmithingRecipe;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.mixin.SmithingTransformRecipeAccessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static smartin.miapi.material.generated.GeneratedMaterialManager.verboseLogging;

/**
 * this class should be able to detect if a smithing recipe exists and then generate it,
 * also checking if the source material is a valid modular material in the process.
 */
public class SmithingRecipeUtil {
    public static void setupSmithingRecipe(List<GeneratedMaterial> materials, boolean isClient, Consumer<GeneratedMaterial> register) {
        try {
            RecipeManager recipeManager = findManager(Environment.isClient());
            RegistryAccess registryAccess = findRegistryManager(isClient);
            if (registryAccess == null) {
                Miapi.LOGGER.warn("Could not setup Smithing Materials, could not find Recipes");
                materials.forEach(register::accept);
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
            if (Miapi.server != null) {
                return Miapi.server.getRecipeManager();
            }
        }
        return null;
    }

    static RegistryAccess findRegistryManager(boolean isClient) {
        if (Miapi.registryAccess != null) {
            return Miapi.registryAccess;
        }
        if (isClient) {
            if (Minecraft.getInstance() != null && Minecraft.getInstance().level != null) {
                return Minecraft.getInstance().level.registryAccess();
            }
        } else {
            if (Miapi.server != null) {
                return Miapi.server.registryAccess();
            }
        }
        return null;
    }

    public static void addSmithingRecipe
            (Material sourceMaterial, GeneratedMaterial outputMaterial, ItemStack templateItem, SmithingTransformRecipe smithingTransformRecipe, RegistryAccess registryAccess, RecipeManager recipeManager) {
        Collection<RecipeHolder<?>> recipes = recipeManager.getRecipes();
        String id = "generated_material_recipe." + outputMaterial.getID() + "." + sourceMaterial.getID() + "." + BuiltInRegistries.ITEM.getKey(templateItem.getItem());
        id = id.replace(":", ".");
        ResourceLocation recipeId = Miapi.id(id);
        if (recipeManager.byKey(recipeId).isEmpty()) {
            MaterialSmithingRecipe materialSmithingRecipe = new MaterialSmithingRecipe(
                    ((SmithingTransformRecipeAccessor) smithingTransformRecipe).getTemplate(),
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
            outputMaterial.setSmithingMaterial(sourceMaterial.getID());
            recipeManager.replaceRecipes(recipes);
        }
    }
}
