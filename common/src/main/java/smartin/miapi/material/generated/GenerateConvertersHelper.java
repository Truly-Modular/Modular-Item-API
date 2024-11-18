package smartin.miapi.material.generated;

import dev.architectury.platform.Platform;
import net.minecraft.world.item.*;
import smartin.miapi.Miapi;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class GenerateConvertersHelper {

    public static void setupTools(List<TieredItem> toolItems, Material material) {
        if (!Platform.isModLoaded("arsenal")) {
            return;
        }
        // Axe
        toolItems.stream()
                .filter(AxeItem.class::isInstance)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if (stack.getItem().equals(item)) {
                            return axeItem(material);
                        }
                        return stack;
                    });
                });

        // Shovel
        toolItems.stream()
                .filter(ShovelItem.class::isInstance)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if (stack.getItem().equals(item)) {
                            return shovelItem(material);
                        }
                        return stack;
                    });
                });

        // Pickaxe
        toolItems.stream()
                .filter(PickaxeItem.class::isInstance)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if (stack.getItem().equals(item)) {
                            return pickaxeItem(material);
                        }
                        return stack;
                    });
                });

        // Hoe
        toolItems.stream()
                .filter(HoeItem.class::isInstance)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if (stack.getItem().equals(item)) {
                            return hoeItem(material);
                        }
                        return stack;
                    });
                });

        // Sword
        toolItems.stream()
                .filter(SwordItem.class::isInstance)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if (stack.getItem().equals(item)) {
                            return swordItem(material);
                        }
                        return stack;
                    });
                });
    }

    public static ItemStack swordItem(Material material) {
        ModuleInstance handleModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "handle/sword")));
        MaterialProperty.setMaterial(handleModule, getWoodMaterial()); // Set material to input material
        // Define the child 'guard' module for the handle
        ModuleInstance guardModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "guard/normal")));
        MaterialProperty.setMaterial(guardModule, material);
        // Define the child 'blade' module for the guard
        ModuleInstance bladeModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "blade/normal")));
        MaterialProperty.setMaterial(bladeModule, material);
        // Set blade as child of guard
        guardModule.setSubModule("blade", bladeModule);
        // Define the 'pommel' module for the handle
        ModuleInstance pommelModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "pommel/round")));
        MaterialProperty.setMaterial(pommelModule, material); // Set pommel material to gold
        // Set guard and pommel as children of handle
        handleModule.setSubModule("guard", guardModule);
        handleModule.setSubModule("pommel", pommelModule);
        // Create the ItemStack for the sword with the handle module configured
        ItemStack swordItem = new ItemStack(RegistryInventory.modularItem); // Use the material specified for the sword base
        handleModule.writeToItem(swordItem);
        swordItem = ItemIdProperty.changeId(swordItem);
        return swordItem;
    }

    public static ItemStack shovelItem(Material material) {
        // Handle module for the shovel
        ModuleInstance handleModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "handle/tool")));
        MaterialProperty.setMaterial(handleModule, getWoodMaterial());

        // Guard module
        ModuleInstance guardModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "guard/tool_adapter")));

        // Tool head module for shovel
        ModuleInstance toolHeadModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/shovel")));
        MaterialProperty.setMaterial(toolHeadModule, material);

        // Set hierarchy
        guardModule.setSubModule("tool_head", toolHeadModule);
        handleModule.setSubModule("guard", guardModule);

        // Create ItemStack
        ItemStack shovelItem = new ItemStack(RegistryInventory.modularItem);
        handleModule.writeToItem(shovelItem);
        shovelItem = ItemIdProperty.changeId(shovelItem);

        return shovelItem;
    }

    public static ItemStack axeItem(Material material) {
        ModuleInstance handleModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "handle/tool")));
        MaterialProperty.setMaterial(handleModule, getWoodMaterial());

        ModuleInstance guardModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "guard/tool_adapter")));

        ModuleInstance toolHeadModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/axe_front")));
        MaterialProperty.setMaterial(toolHeadModule, material);

        ModuleInstance toolBackModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/tool_back")));
        MaterialProperty.setMaterial(toolBackModule, material);

        toolHeadModule.setSubModule("tool_back", toolBackModule);
        guardModule.setSubModule("tool_head", toolHeadModule);
        handleModule.setSubModule("guard", guardModule);

        ItemStack axeItem = new ItemStack(RegistryInventory.modularItem);
        handleModule.writeToItem(axeItem);
        axeItem = ItemIdProperty.changeId(axeItem);

        return axeItem;
    }

    public static ItemStack pickaxeItem(Material material) {
        ModuleInstance handleModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "handle/tool")));
        MaterialProperty.setMaterial(handleModule, getWoodMaterial());

        ModuleInstance guardModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "guard/tool_adapter")));

        ModuleInstance toolHeadModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/pickaxe_front")));
        MaterialProperty.setMaterial(toolHeadModule, material);

        ModuleInstance toolBackModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/pickaxe_back")));
        MaterialProperty.setMaterial(toolBackModule, material);

        toolHeadModule.setSubModule("tool_back", toolBackModule);
        guardModule.setSubModule("tool_head", toolHeadModule);
        handleModule.setSubModule("guard", guardModule);

        ItemStack pickaxeItem = new ItemStack(RegistryInventory.modularItem);
        handleModule.writeToItem(pickaxeItem);
        pickaxeItem = ItemIdProperty.changeId(pickaxeItem);

        return pickaxeItem;
    }

    public static ItemStack hoeItem(Material material) {
        ModuleInstance handleModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "handle/tool")));
        MaterialProperty.setMaterial(handleModule, getWoodMaterial());

        ModuleInstance guardModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "guard/tool_adapter")));

        ModuleInstance toolHeadModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/hoe_front")));
        MaterialProperty.setMaterial(toolHeadModule, material);

        ModuleInstance toolBackModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("arsenal", "tool/tool_back")));
        MaterialProperty.setMaterial(toolBackModule, material);

        toolHeadModule.setSubModule("tool_back", toolBackModule);
        guardModule.setSubModule("tool_head", toolHeadModule);
        handleModule.setSubModule("guard", guardModule);

        ItemStack hoeItem = new ItemStack(RegistryInventory.modularItem);
        handleModule.writeToItem(hoeItem);
        hoeItem = ItemIdProperty.changeId(hoeItem);

        return hoeItem;
    }

    public static Material getWoodMaterial() {
        return MaterialProperty.materials.get(Miapi.id("wood/wood"));
    }


}
