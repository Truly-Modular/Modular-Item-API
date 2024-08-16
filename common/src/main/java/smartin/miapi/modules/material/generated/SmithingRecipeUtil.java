package smartin.miapi.modules.material.generated;

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
import smartin.miapi.mixin.SmithingTransformRecipeAccessor;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static smartin.miapi.modules.material.generated.GeneratedMaterialManager.verboseLogging;

/**
 * this class should be able to detect if a smithing recipe exists and then generate it,
 * also checking if the source material is a valid modular material in the process.
 */
public class SmithingRecipeUtil {
    public static void setupSmithingRecipe(List<GeneratedMaterial> materials) {
        RecipeManager recipeManager = findManager(Environment.isClient());
        RegistryAccess registryAccess = findRegistryManager(Environment.isClient());
        materials.forEach(material -> testForSmithingMaterial(recipeManager,registryAccess,material));
    }

    public static void testForSmithingMaterial(
            RecipeManager manager, RegistryAccess
            registryManager,
            GeneratedMaterial material) {
        manager.getAllRecipesFor(RecipeType.SMITHING).stream()
                .filter(SmithingTransformRecipe.class::isInstance)
                //filter for only ItemChanging Recipes
                .map(SmithingTransformRecipe.class::cast)
                //check if the output is valid
                .filter(recipe -> isValidRecipe(recipe, material.getSwordItem(), registryManager))
                .findAny()
                .ifPresent(smithingTransformRecipe -> {
                    ItemStack templateItem = Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe).getTemplate().getItems()).filter(itemStack -> !itemStack.isEmpty()).findAny().orElse(ItemStack.EMPTY);
                    if (templateItem.isEmpty()) {
                        //make sure the recipe is valid by testing its template Item
                        return;
                    }
                    Arrays.stream(((SmithingTransformRecipeAccessor) smithingTransformRecipe).getBase().getItems())
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
                            .findAny()
                            .ifPresent(sourceMaterial -> {
                                addSmithingRecipe(sourceMaterial, material, templateItem, smithingTransformRecipe, registryManager, manager);
                            });
                });
    }

    static boolean isValidRecipe
            (SmithingTransformRecipe recipe, SwordItem swordItem, RegistryAccess manager) {
        if (recipe.getResultItem(null).getItem().equals(swordItem)) {
            return true;
        }
        return ((SmithingTransformRecipeAccessor) recipe).getResult().getItem().equals(swordItem);
    }

    static RecipeManager findManager(boolean isClient) {
        if (isClient) {
            return Minecraft.getInstance().level.getRecipeManager();
        } else {
            return Miapi.server.getRecipeManager();
        }
    }

    static RegistryAccess findRegistryManager(boolean isClient) {
        if (isClient) {
            return Minecraft.getInstance().level.registryAccess();
        } else {
            return Miapi.server.registryAccess();
        }
    }

    public static void addSmithingRecipe(Material sourceMaterial, GeneratedMaterial outputMaterial, ItemStack templateItem, SmithingTransformRecipe smithingTransformRecipe, RegistryAccess registryAccess, RecipeManager recipeManager) {
        Collection<RecipeHolder<?>> recipes = recipeManager.getRecipes();
        String id = "generated_material_recipe." + outputMaterial.getID() + "." + sourceMaterial.getID() + "." + BuiltInRegistries.ITEM.getKey(templateItem.getItem());
        id = id.replace(":", ".");
        ResourceLocation recipeId = Miapi.id(id);
        if (recipeManager.byKey(recipeId).isEmpty()) {
            MaterialSmithingRecipe materialSmithingRecipe = new MaterialSmithingRecipe(
                    recipeId,
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
