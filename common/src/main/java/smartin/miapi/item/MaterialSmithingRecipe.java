package smartin.miapi.item;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

/**
 * Custom Smithing recipe to replace Materials within a Modular item
 */
public class MaterialSmithingRecipe implements SmithingRecipe {

    final String startMaterial;
    final String resultMaterial;
    final Ingredient smithingTemplate;
    final Ingredient addition;
    final Identifier id;

    public MaterialSmithingRecipe(Identifier id, Ingredient template, String base, Ingredient addition, String resultMaterial) {
        this.startMaterial = base;
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
    public boolean testTemplate(ItemStack stack) {
        Item netherIteUpgradeTemplate = Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
        return stack.getItem().equals(netherIteUpgradeTemplate);
    }

    /**
     * Checks if the Item to be modified and replaced contains the required materials {@link MaterialSmithingRecipe#startMaterial}
     *
     * @param stack the stack to be tested
     * @return if the stack has the material
     */
    @Override
    public boolean testBase(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            ItemModule.ModuleInstance instance = ItemModule.getModules(stack);
            return instance.allSubModules().stream().anyMatch(module -> {
                Material material = MaterialProperty.getMaterial(module);
                if(material!=null){
                    return material.getKey().equals(startMaterial);
                }
                return false;
            });
        }
        return false;
    }

    /**
     * if the Material to be added is the correct ingrediant {@link MaterialSmithingRecipe#addition}
     *
     * @param stack the stack to be tested
     * @return if the stack is of the right ingredient
     */
    @Override
    public boolean testAddition(ItemStack stack) {
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
    public boolean matches(Inventory inventory, World world) {
        return testTemplate(inventory.getStack(0)) && testBase(inventory.getStack(1)) && addition.test(inventory.getStack(2));
    }

    /**
     * executes the craft action. does not change the inventory.
     *
     * @param inventory       the input inventory
     * @param registryManager
     * @return the crafted stack
     */
    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        ItemStack old = inventory.getStack(1).copy();
        if (old.getItem() instanceof ModularItem) {
            ItemModule.ModuleInstance instance = ItemModule.getModules(old).copy();
            instance.allSubModules().forEach(module -> {
                Material material = MaterialProperty.getMaterial(module);
                if (material.getKey().equals(startMaterial)) {
                    MaterialProperty.setMaterial(module, resultMaterial);
                }
            });
            instance.writeToItem(old);
        }
        return old;
    }

    /**
     * Not used by vanilla.
     * This is meant to change the inventory and adjust the inputs.
     * But Mojang decided that Smithing recipes dont need to be able to do that.
     *
     * @param inventory the input inventory
     * @return
     */
    @Override
    public DefaultedList<ItemStack> getRemainder(Inventory inventory) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            Item item = inventory.getStack(i).getItem();
            //if (!item.hasRecipeRemainder()) continue;
            //defaultedList.set(i, inventory.getStack(i).copy());
        }
        //defaultedList.set(1, ItemStack.EMPTY);
        return defaultedList;
    }

    /**
     * Returns a preview output without context.
     *
     * @param registryManager
     * @return an empty itemstack since we dont know
     */
    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    /**
     * The Id of the recipe
     *
     * @return
     */
    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryInventory.serializer;
    }

    public static class Serializer
            implements RecipeSerializer<MaterialSmithingRecipe> {
        @Override
        public MaterialSmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
            Ingredient template = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "template"));
            Ingredient addition = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "addition"));
            String base = jsonObject.get("base").getAsString();
            String result = jsonObject.get("result").getAsString();
            return new MaterialSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public MaterialSmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient template = Ingredient.fromPacket(packetByteBuf);
            Ingredient addition = Ingredient.fromPacket(packetByteBuf);
            String base = packetByteBuf.readString();
            String result = packetByteBuf.readString();
            return new MaterialSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public void write(PacketByteBuf packetByteBuf, MaterialSmithingRecipe smithingTransformRecipe) {
            smithingTransformRecipe.smithingTemplate.write(packetByteBuf);
            smithingTransformRecipe.addition.write(packetByteBuf);
            packetByteBuf.writeString(smithingTransformRecipe.startMaterial);
            packetByteBuf.writeString(smithingTransformRecipe.resultMaterial);
        }
    }
}
