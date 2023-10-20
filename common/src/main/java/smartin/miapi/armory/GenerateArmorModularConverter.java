package smartin.miapi.armory;

import dev.architectury.event.EventResult;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.GeneratedMaterial;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.*;

public class GenerateArmorModularConverter {
    public Map<Item, Converter> modularItem = new HashMap<>();
    public Map<ArmorMaterial, List<ArmorItem>> armorItems = new HashMap<>();

    public GenerateArmorModularConverter() {
        ReloadEvents.START.subscribe(isClient -> {
            modularItem.clear();
            armorItems.clear();
            Registries.ITEM.stream().filter(ArmorItem.class::isInstance).forEach(item -> {
                if(item instanceof ArmorItem armorItem && armorItem.getMaterial() != null && armorItem.getType() != null && armorItem.getMaterial().getRepairIngredient() != null){
                    List<ArmorItem> armorItems1 = armorItems.getOrDefault((armorItem).getMaterial(), new ArrayList<>());
                    armorItems1.add(armorItem);
                    armorItems.put((armorItem).getMaterial(),armorItems1);
                }
            });
        });
        ModularItemStackConverter.converters.add((stack) -> {
            if (modularItem.containsKey(stack.getItem())) {
                return ItemIdProperty.changeId(modularItem.get(stack.getItem()).convert(stack));
            }
            return stack;
        });
        MiapiEvents.GENERATED_MATERIAL.register((GeneratedMaterial material, ItemStack mainIngredient, List<Item> tools, boolean isClient) -> {
            Optional<ArmorMaterial> armorMaterial = armorItems.keySet().stream().filter(armor -> armor.getRepairIngredient().test(mainIngredient)).findFirst();
            if(armorMaterial.isPresent()){
                List<ArmorItem> materialArmorItems = armorItems.get(armorMaterial.get());
                Optional<ArmorItem> helmetItem = materialArmorItems.stream().filter(item -> item.getType().equals(ArmorItem.Type.HELMET)).findFirst();
                Optional<ArmorItem> chestPlateItem = materialArmorItems.stream().filter(item -> item.getType().equals(ArmorItem.Type.CHESTPLATE)).findFirst();
                Optional<ArmorItem> leggingsItem = materialArmorItems.stream().filter(item -> item.getType().equals(ArmorItem.Type.LEGGINGS)).findFirst();
                Optional<ArmorItem> shoeItem = materialArmorItems.stream().filter(item -> item.getType().equals(ArmorItem.Type.BOOTS)).findFirst();

                helmetItem.ifPresent(item -> addHelmetItem(material, item));
                chestPlateItem.ifPresent(item -> addChestPlateItem(material, item));
                leggingsItem.ifPresent(item -> addLeggingsItem(material, item));
                shoeItem.ifPresent(item -> addShoesItem(material, item));
            }
            return EventResult.pass();
        });
    }

    interface Converter {
        ItemStack convert(ItemStack raw);
    }

    protected void addHelmetItem(Material material, Item item) {
        modularItem.put(item, (stack) -> {
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"helmet_base\",\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"helmet_plate\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold", material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addChestPlateItem(Material material, Item item) {
        modularItem.put(item, (stack) -> {
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"chest_base\",\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"front_chestplate\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            },\n" +
                    "            \"1\": {\n" +
                    "                \"module\": \"arm_left\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            },\n" +
                    "            \"2\": {\n" +
                    "                \"module\": \"arm_right\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold", material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addLeggingsItem(Material material, Item item) {
        modularItem.put(item, (stack) -> {
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"pants_base\",\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"belt_base\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            },\n" +
                    "            \"1\": {\n" +
                    "                \"module\": \"leg_left\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            },\n" +
                    "            \"2\": {\n" +
                    "                \"module\": \"leg_right\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold", material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addShoesItem(Material material, Item item) {
        modularItem.put(item, (stack) -> {
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"boot_base\",\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"boot_left\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            },\n" +
                    "            \"1\": {\n" +
                    "                \"module\": \"boot_right\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold", material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }
}
