package smartin.miapi.item;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.VisualModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

/**
 * Custom Smithing recipe to replace Materials within a Modular item
 */
public class MaterialSmithingRecipe implements SmithingRecipe {

    final String startMaterial;
    final String resultMaterial;
    final Ingredient smithingTemplate;
    final Ingredient addition;
    final ResourceLocation id;

    public MaterialSmithingRecipe(ResourceLocation id, Ingredient template, String baseMaterial, Ingredient addition, String resultMaterial) {
        this.startMaterial = baseMaterial;
        this.resultMaterial = resultMaterial;
        smithingTemplate = template;
        this.addition = addition;
        this.id = id;
    }

    /**
     * Checks against the smithingTemplate of {@link MaterialSmithingRecipe#smithingTemplate}
     *
     * @param stack the stack to be tested
     * @return if the stack parses the ingredient
     */
    @Override
    public boolean isTemplateIngredient(ItemStack stack) {
        return smithingTemplate.test(stack);
    }

    /**
     * Checks if the Item to be modified and replaced contains the required materials {@link MaterialSmithingRecipe#startMaterial}
     *
     * @param stack the stack to be tested
     * @return if the stack has the material
     */
    @Override
    public boolean isBaseIngredient(ItemStack stack) {
        if (stack.getItem() instanceof VisualModularItem) {
            ModuleInstance instance = ItemModule.getModules(stack);
            return instance.allSubModules().stream().anyMatch(module -> {
                Material material = MaterialProperty.getMaterial(module);
                if (material != null) {
                    return material.getKey().equals(startMaterial);
                }
                return false;
            });
        }
        return false;
    }

    /**
     * if the Material to be added is the correct ingredient {@link MaterialSmithingRecipe#addition}
     *
     * @param stack the stack to be tested
     * @return if the stack is of the right ingredient
     */
    @Override
    public boolean isAdditionIngredient(ItemStack stack) {
        return addition.test(stack);
    }

    /**
     * Checks if the inventory contains all the required items to be crafted
     *
     * @param inventory the input inventory
     * @param world     the input world
     * @return
     */
    @Override
    public boolean matches(Container inventory, Level world) {
        return isTemplateIngredient(inventory.getItem(0)) && isBaseIngredient(inventory.getItem(1)) && addition.test(inventory.getItem(2));
    }

    /**
     * executes the craft action. does not change the inventory.
     *
     * @param inventory       the input inventory
     * @param registryManager
     * @return the crafted stack
     */
    @Override
    public ItemStack craft(Container inventory, RegistryAccess registryManager) {
        ItemStack old = inventory.getItem(1).copy();
        if (old.getItem() instanceof VisualModularItem) {
            ModuleInstance instance = ItemModule.getModules(old).copy();
            instance.allSubModules().forEach(module -> {
                Material material = MaterialProperty.getMaterial(module);
                if (material != null && material.getKey().equals(startMaterial)) {
                    MaterialProperty.setMaterial(module, resultMaterial);

                }
            });
            instance.writeToItem(old);
        }
        MiapiEvents.MaterialCraft data = new MiapiEvents.MaterialCraft(old);
        MiapiEvents.SMITHING_EVENT.invoker().craft(data);
        return data.itemStack;
    }

    /**
     * Returns a previewStack output without context.
     *
     * @param registryManager
     * @return an empty itemstack since we dont know
     */
    @Override
    public ItemStack getOutput(RegistryAccess registryManager) {
        return ItemStack.EMPTY;
    }

    /**
     * The Id of the recipe
     *
     * @return
     */
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryInventory.serializer;
    }

    public static class Serializer
            implements RecipeSerializer<MaterialSmithingRecipe> {
        @Override
        public MaterialSmithingRecipe read(ResourceLocation identifier, JsonObject jsonObject) {
            Ingredient template = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "template"));
            Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(jsonObject, "addition"));
            String base = jsonObject.get("base").getAsString();
            String result = jsonObject.get("result").getAsString();
            return new MaterialSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public MaterialSmithingRecipe read(ResourceLocation identifier, FriendlyByteBuf packetByteBuf) {
            Ingredient template = Ingredient.fromPacket(packetByteBuf);
            Ingredient addition = Ingredient.fromPacket(packetByteBuf);
            String base = packetByteBuf.readUtf();
            String result = packetByteBuf.readUtf();
            return new MaterialSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public void write(FriendlyByteBuf packetByteBuf, MaterialSmithingRecipe smithingTransformRecipe) {
            smithingTransformRecipe.smithingTemplate.write(packetByteBuf);
            smithingTransformRecipe.addition.write(packetByteBuf);
            packetByteBuf.writeUtf(smithingTransformRecipe.startMaterial);
            packetByteBuf.writeUtf(smithingTransformRecipe.resultMaterial);
        }
    }
}
