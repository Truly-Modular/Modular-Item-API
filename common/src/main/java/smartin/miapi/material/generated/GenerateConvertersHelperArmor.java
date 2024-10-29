package smartin.miapi.material.generated;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.item.ModularItemStackConverter;
import smartin.miapi.material.Material;
import smartin.miapi.material.MaterialProperty;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.ItemIdProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

public class GenerateConvertersHelperArmor {

    public static void setup(List<ArmorItem> armorItems, Material material) {
        if(!Platform.isModLoaded("armory")){
            return;
        }
        // Helmet
        armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.HEAD)
                .map(a -> (Item)a)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if(stack.getItem().equals(item)){
                            return helmetItem(material);
                        }
                        return stack;
                    });
                });

        // Chestplate
        armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.CHEST)
                .map(a -> (Item)a)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if(stack.getItem().equals(item)){
                            return chestplateItem(material);
                        }
                        return stack;
                    });
                });

        // Leggings
        armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.LEGS)
                .map(a -> (Item)a)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if(stack.getItem().equals(item)){
                            return leggingsItem(material);
                        }
                        return stack;
                    });
                });

        // Boots
        armorItems.stream()
                .filter(a -> a.getEquipmentSlot() == EquipmentSlot.FEET)
                .map(a -> (Item)a)
                .findFirst()
                .ifPresent(item -> {
                    ModularItemStackConverter.converters.add(stack -> {
                        if(stack.getItem().equals(item)){
                            return bootsItem(material);
                        }
                        return stack;
                    });
                });
    }


    public static ItemStack bootsItem(Material material) {
        ModuleInstance bootsModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/boots")));

        ModuleInstance leftBootModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/boot_left")));
        MaterialProperty.setMaterial(leftBootModule, material);

        ModuleInstance rightBootModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/boot_right")));
        MaterialProperty.setMaterial(rightBootModule, material);

        bootsModule.setSubModule("boot_left", leftBootModule);
        bootsModule.setSubModule("boot_right", rightBootModule);

        ItemStack bootsItem = new ItemStack(RegistryInventory.modularItem);
        bootsModule.writeToItem(bootsItem);
        bootsItem = ItemIdProperty.changeId(bootsItem);

        return bootsItem;
    }

    public static ItemStack leggingsItem(Material material) {
        ModuleInstance leggingsModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/pants")));

        ModuleInstance beltModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/belt")));
        MaterialProperty.setMaterial(beltModule, material);

        ModuleInstance leftLegModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/leg_left")));
        MaterialProperty.setMaterial(leftLegModule, material);

        ModuleInstance rightLegModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/leg_right")));
        MaterialProperty.setMaterial(rightLegModule, material);

        leggingsModule.setSubModule("belt", beltModule);
        leggingsModule.setSubModule("leg_left", leftLegModule);
        leggingsModule.setSubModule("leg_right", rightLegModule);

        ItemStack leggingsItem = new ItemStack(RegistryInventory.modularItem);
        leggingsModule.writeToItem(leggingsItem);
        leggingsItem = ItemIdProperty.changeId(leggingsItem);

        return leggingsItem;
    }

    public static ItemStack chestplateItem(Material material) {
        ModuleInstance chestplateModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/chestplate")));

        ModuleInstance frontChestModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/front_chest")));
        MaterialProperty.setMaterial(frontChestModule, material);

        ModuleInstance backChestModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/back_chest")));
        MaterialProperty.setMaterial(backChestModule, material);

        ModuleInstance leftArmModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/arm_left")));
        MaterialProperty.setMaterial(leftArmModule, material);

        ModuleInstance rightArmModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/arm_right")));
        MaterialProperty.setMaterial(rightArmModule, material);

        chestplateModule.setSubModule("chest_front", frontChestModule);
        chestplateModule.setSubModule("chest_back", backChestModule);
        chestplateModule.setSubModule("arm_left", leftArmModule);
        chestplateModule.setSubModule("arm_right", rightArmModule);

        ItemStack chestplateItem = new ItemStack(RegistryInventory.modularItem);
        chestplateModule.writeToItem(chestplateItem);
        chestplateItem = ItemIdProperty.changeId(chestplateItem);

        return chestplateItem;
    }

    public static ItemStack helmetItem(Material material) {
        // Create the root module for the helmet
        ModuleInstance helmetModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/helmet")));

        // Create the "hat" submodule representing the actual helmet part
        ModuleInstance hatModule = new ModuleInstance(RegistryInventory.modules.get(Miapi.id("armory", "armor/default/helmet")));
        MaterialProperty.setMaterial(hatModule, material);

        // Set "hat" as a child of "helmet"
        helmetModule.setSubModule("hat", hatModule);

        // Create the ItemStack for the helmet with the configured module
        ItemStack helmetItem = new ItemStack(RegistryInventory.modularItem);
        helmetModule.writeToItem(helmetItem);
        helmetItem = ItemIdProperty.changeId(helmetItem);

        return helmetItem;
    }

}
