package smartin.miapi.item;

import com.mojang.serialization.MapCodec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
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

    public static MapCodec<MaterialSmithingRecipe> CODEC = AutoCodec.of(MaterialSmithingRecipe.class);

    public final String startMaterial;
    public final String resultMaterial;
    public final Ingredient smithingTemplate;
    public final Ingredient addition;
    public final ResourceLocation id;

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
    public boolean matches(SmithingRecipeInput inventory, Level world) {
        return isTemplateIngredient(inventory.getItem(0)) && isBaseIngredient(inventory.getItem(1)) && addition.test(inventory.getItem(2));
    }

    /**
     * @return the crafted stack
     */
    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack old = input.getItem(1).copy();
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
     * @return an empty itemstack since we dont know
     */
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryInventory.serializer;
    }

    public static class Serializer
            implements RecipeSerializer<MaterialSmithingRecipe> {
        @Override
        public MapCodec<MaterialSmithingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MaterialSmithingRecipe> streamCodec() {
            return ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
        }
    }
}
