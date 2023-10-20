package smartin.miapi.arsenal;

import dev.architectury.event.EventResult;
import net.minecraft.item.*;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.GeneratedMaterial;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenerateModularConverters {
    public Map<Item, Converter> modularItem = new HashMap();

    public GenerateModularConverters() {
        ReloadEvents.START.subscribe(isClient -> {
            modularItem.clear();
        });
        ModularItemStackConverter.converters.add((stack)->{
            if(modularItem.containsKey(stack.getItem())){
                return ItemIdProperty.changeId(modularItem.get(stack.getItem()).convert(stack));
            }
            return stack;
        });
        MiapiEvents.GENERATED_MATERIAL.register((GeneratedMaterial material, ItemStack mainIngredient, List<Item> tools, boolean isClient) -> {
            Optional<Item> swordItem = tools.stream().filter(SwordItem.class::isInstance).findFirst();
            Optional<Item> axeItem = tools.stream().filter(AxeItem.class::isInstance).findFirst();
            Optional<Item> pickAxeItem = tools.stream().filter(PickaxeItem.class::isInstance).findFirst();
            Optional<Item> shovelItem = tools.stream().filter(ShovelItem.class::isInstance).findFirst();
            Optional<Item> hoeItem = tools.stream().filter(HoeItem.class::isInstance).findFirst();
            swordItem.ifPresent(item -> addSwordItem(material, item));
            axeItem.ifPresent(item -> addAxeItem(material, item));
            pickAxeItem.ifPresent(item -> addPickAxeItem(material, item));
            shovelItem.ifPresent(item -> addShovelItem(material, item));
            hoeItem.ifPresent(item -> addHoeItem(material, item));
            return EventResult.pass();
        });
    }

    interface Converter{
        ItemStack convert(ItemStack raw);
    }

    protected void addSwordItem(Material material, Item item) {
        modularItem.put(item,(stack)->{
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = new StringBuilder()
                    .append("{\n")
                    .append("        \"module\": \"handle_normal\",\n")
                    .append("        \"moduleData\": {\n")
                    .append("            \"properties\": \"{\\\"material\\\":\\\"wood\\\"}\"\n")
                    .append("        },\n")
                    .append("        \"subModules\": {\n")
                    .append("            \"0\": {\n")
                    .append("                \"module\": \"guard_normal\",\n")
                    .append("                \"moduleData\": {\n")
                    .append("                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n")
                    .append("                },\n")
                    .append("                \"subModules\": {\n").
                    append("                    \"0\": {\n")
                    .append("                        \"module\": \"blade_sword\",\n")
                    .append("                        \"moduleData\": {\n")
                    .append("                            \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n")
                    .append("                        },\n")
                    .append("                        \"subModules\": {}\n")
                    .append("                    }\n")
                    .append("                }\n")
                    .append("            },\n")
                    .append("            \"1\": {\n")
                    .append("                \"module\": \"pommel_round\",\n")
                    .append("                \"moduleData\": {\n")
                    .append("                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n")
                    .append("                },\n")
                    .append("                \"subModules\": {}\n")
                    .append("            }\n")
                    .append("        }\n")
                    .append("    }").toString();
            swordData = swordData.replaceAll("gold",material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addAxeItem(Material material, Item item) {
        modularItem.put(item,(stack)->{
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"handle_tool\",\n" +
                    "        \"moduleData\": {\n" +
                    "            \"properties\": \"{\\\"material\\\":\\\"wood\\\"}\"\n" +
                    "        },\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"axe_front\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {\n" +
                    "                    \"0\": {\n" +
                    "                        \"module\": \"tool_back\",\n" +
                    "                        \"moduleData\": {\n" +
                    "                            \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                        },\n" +
                    "                        \"subModules\": {}\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold",material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addPickAxeItem(Material material, Item item) {
        modularItem.put(item,(stack)->{
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"handle_tool\",\n" +
                    "        \"moduleData\": {\n" +
                    "            \"properties\": \"{\\\"material\\\":\\\"wood\\\"}\"\n" +
                    "        },\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"pickaxe_front\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {\n" +
                    "                    \"0\": {\n" +
                    "                        \"module\": \"pickaxe_back\",\n" +
                    "                        \"moduleData\": {\n" +
                    "                            \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                        },\n" +
                    "                        \"subModules\": {}\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold",material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addShovelItem(Material material, Item item) {
        modularItem.put(item,(stack)->{
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"handle_tool\",\n" +
                    "        \"moduleData\": {\n" +
                    "            \"properties\": \"{\\\"material\\\":\\\"wood\\\"}\"\n" +
                    "        },\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"shovel\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {}\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold",material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }

    protected void addHoeItem(Material material, Item item) {
        modularItem.put(item,(stack)->{
            ItemStack modularItem = new ItemStack(RegistryInventory.modularItem);
            String swordData = "{\n" +
                    "        \"module\": \"handle_tool\",\n" +
                    "        \"moduleData\": {\n" +
                    "            \"properties\": \"{\\\"material\\\":\\\"wood\\\"}\"\n" +
                    "        },\n" +
                    "        \"subModules\": {\n" +
                    "            \"0\": {\n" +
                    "                \"module\": \"hoe_front\",\n" +
                    "                \"moduleData\": {\n" +
                    "                    \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                },\n" +
                    "                \"subModules\": {\n" +
                    "                    \"0\": {\n" +
                    "                        \"module\": \"tool_back\",\n" +
                    "                        \"moduleData\": {\n" +
                    "                            \"properties\": \"{\\\"material\\\":\\\"gold\\\"}\"\n" +
                    "                        },\n" +
                    "                        \"subModules\": {}\n" +
                    "                    }\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }";
            swordData = swordData.replaceAll("gold",material.getKey());
            modularItem.getOrCreateNbt().copyFrom(stack.getOrCreateNbt());
            ItemModule.ModuleInstance moduleInstance = ItemModule.ModuleInstance.fromString(swordData);
            moduleInstance.getRoot().writeToItem(modularItem);
            return modularItem;
        });
    }
}
