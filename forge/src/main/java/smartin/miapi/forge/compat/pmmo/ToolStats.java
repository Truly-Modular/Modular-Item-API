package smartin.miapi.forge.compat.pmmo;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraftforge.fml.LogicalSide;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.forge.mixin.AutoItemAccessor;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.material.Material;
import smartin.miapi.modules.material.MaterialProperty;
import smartin.miapi.modules.properties.mining.MiningLevelProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ToolStats {
    public static void setup() {
        ReloadEvents.END.subscribe((ToolStats::register));
    }

    public static void register(boolean isClient) {
        RegistryInventory.modularItems.addCallback((item -> {
            Identifier itemID = RegistryInventory.modularItems.getId(item);
            if (item instanceof ArmorItem || item instanceof ElytraItem) {
                setupForItem(itemID, ReqType.WEAR, false, isClient);
            } else {
                setupForItem(itemID, ReqType.TOOL, false, isClient);
                setupForItem(itemID, ReqType.WEAPON, true, isClient);
            }
        }));
    }

    public static void registerItemRequirementTooltipData(Identifier res, ReqType reqType, boolean isClient, Function<ItemStack, Map<String, Integer>> func) {
        if (isClient) {
            Core.get(LogicalSide.CLIENT).getTooltipRegistry().registerItemRequirementTooltipData(res, reqType, func);
        } else {
            Core.get(LogicalSide.SERVER).getTooltipRegistry().registerItemRequirementTooltipData(res, reqType, func);
        }
    }

    private static void setupForItem(Identifier identifier, ReqType tool, boolean isWeapon, boolean isClient) {
        registerItemRequirementTooltipData(identifier, tool, isClient, (stack -> {
            Map<String, Integer> outMap = new HashMap<>();
            try {
                //Miapi.LOGGER.info("damage " + stack.getItem() + " " + AutoItemAccessor.callGetDamage(stack));
                if (stack.getItem() instanceof SwordItem) {
                    //Miapi.LOGGER.info("damage " + AutoItemAccessor.callGetDamage(stack));
                    outMap.put("combat", (int) getUtensilAttributes(AutoValueConfig.UtensilTypes.SWORD, stack, true));
                    //Miapi.LOGGER.info("value " + getUtensilAttributes(AutoValueConfig.UtensilTypes.SWORD, stack, true));
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.SWORD, tool, stack, true));
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.SWORD, tool, stack, false));
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.AXE, tool, stack, true));
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.AXE, tool, stack, false));
                } else if (stack.getItem() instanceof AxeItem) {
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.AXE, tool, stack, isWeapon));
                } else if (stack.getItem() instanceof PickaxeItem) {
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.PICKAXE, tool, stack, isWeapon));
                } else if (stack.getItem() instanceof ShovelItem) {
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.SHOVEL, tool, stack, isWeapon));
                } else if (stack.getItem() instanceof HoeItem) {
                    outMap.putAll(AutoItemAccessor.callGetUtensilData(AutoValueConfig.UtensilTypes.HOE, tool, stack, isWeapon));
                } else if (stack.getItem() instanceof ArmorItem && tool == ReqType.WEAR) {
                    outMap.putAll(AutoItemAccessor.callGetWearableData(tool, stack, true));
                } else if (stack.getItem() instanceof ElytraItem && tool == ReqType.WEAR) {
                    outMap.putAll(AutoItemAccessor.callGetWearableData(tool, stack, true));
                } else {
                    outMap.put("combat", (int) getUtensilAttributes(AutoValueConfig.UtensilTypes.SWORD, stack, true));
                }
            } catch (RuntimeException e) {

            }
            return outMap;
        }));
    }

    private static double getUtensilAttributes(AutoValueConfig.UtensilTypes type, ItemStack stack, boolean asWeapon) {
        // Fetch base values
        double baseDurability = AutoItemAccessor.callGetDurability(stack);
        double baseTier = MiningLevelProperty.getMiningLevel("sword", stack);
        double baseDamage = asWeapon ? AutoItemAccessor.callGetDamage(stack) : 0.0;
        double baseAtkSpeed = asWeapon ? AutoItemAccessor.callGetAttackSpeed(stack) : 0.0;
        double baseDigSpeed = 15;

        // Fetch multipliers
        double durMultiplier = AutoValueConfig.getUtensilAttribute(type, AutoValueConfig.AttributeKey.DUR);
        double tierMultiplier = AutoValueConfig.getUtensilAttribute(type, AutoValueConfig.AttributeKey.TIER);
        double dmgMultiplier = asWeapon ? AutoValueConfig.getUtensilAttribute(type, AutoValueConfig.AttributeKey.DMG) : 0.0;
        double spdMultiplier = asWeapon ? AutoValueConfig.getUtensilAttribute(type, AutoValueConfig.AttributeKey.SPD) : 0.0;
        double digMultiplier = asWeapon ? 0.0 : AutoValueConfig.getUtensilAttribute(type, AutoValueConfig.AttributeKey.DIG);

        baseTier = 0;
        for (ItemModule.ModuleInstance moduleInstance : ItemModule.getModules(stack).allSubModules()) {
            Material material = MaterialProperty.getMaterial(moduleInstance);
            if (material != null) {
                baseTier = Math.max(baseTier, MaterialProperty.getMaterial(moduleInstance).getDouble("mining_level"));
            }
        }

        // Calculate scaled values
        double durabilityScale = baseDurability * durMultiplier;
        double tierScale = baseTier * tierMultiplier;
        double damageScale = baseDamage * dmgMultiplier;
        double atkSpdScale = baseAtkSpeed * spdMultiplier;
        double digSpeedScale = baseDigSpeed + digMultiplier;

        /*
        Miapi.LOGGER.info("AutoItem Base Values: DUR=" + baseDurability + " TIER=" + baseTier +
                          " DMG=" + baseDamage + " SPD=" + baseAtkSpeed + " DIG=" + baseDigSpeed);
        Miapi.LOGGER.info("AutoItem Multipliers: DUR=" + durMultiplier + " TIER=" + tierMultiplier +
                          " DMG=" + dmgMultiplier + " SPD=" + spdMultiplier + " DIG=" + digMultiplier);

        // Log the scaled values
        Miapi.LOGGER.info("AutoItem Scaled Values: DUR=" + durabilityScale + " TIER=" + tierScale +
                          " DMG=" + damageScale + " SPD=" + atkSpdScale + " DIG=" + digSpeedScale);

         */


        return damageScale + atkSpdScale + digSpeedScale + durabilityScale + tierScale;
    }

}
