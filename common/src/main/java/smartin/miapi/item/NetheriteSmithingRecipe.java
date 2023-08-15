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
import net.minecraft.world.World;
import smartin.miapi.Miapi;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.modules.properties.material.MaterialProperty;
import smartin.miapi.registries.RegistryInventory;

public class NetheriteSmithingRecipe implements SmithingRecipe {

    final String startMaterial;
    final String resultMaterial;
    final Ingredient smithingTemplate;
    final Ingredient addition;
    final Identifier id;

    public NetheriteSmithingRecipe(Identifier id, Ingredient template, String base, Ingredient addition, String resultMaterial) {
        this.startMaterial = base;
        this.resultMaterial = resultMaterial;
        smithingTemplate = template;
        this.addition = addition;
        this.id = id;
    }

    @Override
    public boolean testTemplate(ItemStack stack) {
        Item netherIteUpgradeTemplate = Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
        return stack.getItem().equals(netherIteUpgradeTemplate);
    }

    @Override
    public boolean testBase(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            ItemModule.ModuleInstance instance = ItemModule.getModules(stack);
            return instance.allSubModules().stream().anyMatch(module -> {
                Material material = MaterialProperty.getMaterial(module);
                Miapi.LOGGER.warn(material.getKey());
                return material.getKey().equals(startMaterial);
            });
        }
        return false;
    }

    @Override
    public boolean testAddition(ItemStack stack) {
        return addition.test(stack);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return testTemplate(inventory.getStack(0)) && testBase(inventory.getStack(1)) && addition.test(inventory.getStack(2));
    }

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

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return new ItemStack(RegistryInventory.modularItem);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RegistryInventory.serializer;
    }

    public static class Serializer
            implements RecipeSerializer<NetheriteSmithingRecipe> {
        @Override
        public NetheriteSmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
            Ingredient template = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "template"));
            Ingredient addition = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "addition"));
            String base = jsonObject.get("base").getAsString();
            String result = jsonObject.get("result").getAsString();
            return new NetheriteSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public NetheriteSmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient template = Ingredient.fromPacket(packetByteBuf);
            Ingredient addition = Ingredient.fromPacket(packetByteBuf);
            String base = packetByteBuf.readString();
            String result = packetByteBuf.readString();
            return new NetheriteSmithingRecipe(identifier, template, base, addition, result);
        }

        @Override
        public void write(PacketByteBuf packetByteBuf, NetheriteSmithingRecipe smithingTransformRecipe) {
            smithingTransformRecipe.smithingTemplate.write(packetByteBuf);
            smithingTransformRecipe.addition.write(packetByteBuf);
            packetByteBuf.writeString(smithingTransformRecipe.startMaterial);
            packetByteBuf.writeString(smithingTransformRecipe.resultMaterial);
        }
    }
}
