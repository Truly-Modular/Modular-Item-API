package smartin.miapi.registries;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import static smartin.miapi.attributes.AttributeRegistry.*;

public class AttributeRegistry {
    static boolean init = false;

    public static void registerAttributes() {
        if (init) {
            return;
        }
        init = true;
        //ATTRIBUTE
        if(Platform.isFabric()){
            RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                            new RangedAttribute("miapi.attribute.name.swim_speed", 1.0, 0.01, 1024.0).setSyncable(true),
                    att -> SWIM_SPEED = att);
        }

        // mining
        RegistryInventory.registerAtt("remove.mining_speed.pickaxe", false, () ->
                        new RangedAttribute("miapi.attribute.name.mining_speed.pickaxe", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> MINING_SPEED_PICKAXE = att);
        RegistryInventory.registerAtt("remove.mining_speed.axe", false, () ->
                        new RangedAttribute("miapi.attribute.name.mining_speed.axe", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> MINING_SPEED_AXE = att);
        RegistryInventory.registerAtt("remove.mining_speed.shovel", false, () ->
                        new RangedAttribute("miapi.attribute.name.mining_speed.shovel", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> MINING_SPEED_SHOVEL = att);
        RegistryInventory.registerAtt("remove.mining_speed.hoe", false, () ->
                        new RangedAttribute("miapi.attribute.name.mining_speed.hoe", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> MINING_SPEED_HOE = att);

        // entity attached
        RegistryInventory.registerAtt("generic.resistance", true, () ->
                        new RangedAttribute("miapi.attribute.name.resistance", 0.0, 0.0, 100).setSyncable(true),
                att -> DAMAGE_RESISTANCE = att);
        RegistryInventory.registerAtt("generic.back_stab", true, () ->
                        new RangedAttribute("miapi.attribute.name.back_stab", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> BACK_STAB = att);
        RegistryInventory.registerAtt("generic.armor_crushing", true, () ->
                        new RangedAttribute("miapi.attribute.name.armor_crushing", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> ARMOR_CRUSHING = att);
        RegistryInventory.registerAtt("generic.projectile_armor", true, () ->
                        new RangedAttribute("miapi.attribute.name.projectile_armor", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> PROJECTILE_ARMOR = att);
        RegistryInventory.registerAtt("generic.shield_break", true, () ->
                        new RangedAttribute("miapi.attribute.name.shield_break", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> SHIELD_BREAK = att);

        RegistryInventory.registerAtt("generic.player_item_use_speed", true, () ->
                        new RangedAttribute("miapi.attribute.name.player_item_use_speed", -0.8, -1.0, 0.0).setSyncable(true),
                att -> PLAYER_ITEM_USE_MOVEMENT_SPEED = att);

        RegistryInventory.registerAtt("generic.magic_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.magic_damage", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> MAGIC_DAMAGE = att);
        RegistryInventory.registerAtt("generic.true_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.true_damage", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> TRUE_DAMAGE = att);
        RegistryInventory.registerAtt("generic.armor_pierce_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.armor_pierce_damage", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> ARMOR_PIERCE_DAMAGE = att);

        RegistryInventory.registerAtt("generic.stun_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.stun_damage", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> STUN_DAMAGE = att);

        RegistryInventory.registerAtt("generic.stun_max_health", true, () ->
                        new RangedAttribute("miapi.attribute.name.stun_max_health",
                                //MiapiConfig.getServerConfig().stunEffectCategory.stunHealth
                                20.0,
                                0.0, 1024.0).setSyncable(true),
                att -> STUN_MAX_HEALTH = att);

        RegistryInventory.registerAtt("generic.crit_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.crit_damage", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> CRITICAL_DAMAGE = att);

        RegistryInventory.registerAtt("generic.crit_chance", true, () ->
                        new RangedAttribute("miapi.attribute.name.crit_chance", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> CRITICAL_CHANCE = att);

        //projectile based
        RegistryInventory.registerAtt("generic.projectile_damage", true, () ->
                        new RangedAttribute("miapi.attribute.name.projectile_damage", 0.0, -1024.0, 1024.0).setSyncable(true),
                att -> PROJECTILE_DAMAGE = att);
        RegistryInventory.registerAtt("generic.projectile_speed", true, () ->
                        new RangedAttribute("miapi.attribute.name.projectile_speed", 0.0, -1024.0, 1024.0).setSyncable(true),
                att -> PROJECTILE_SPEED = att);
        RegistryInventory.registerAtt("generic.projectile_accuracy", true, () ->
                        new RangedAttribute("miapi.attribute.name.projectile_accuracy", 0.0, -1024.0, 1024.0).setSyncable(true),
                att -> PROJECTILE_ACCURACY = att);
        RegistryInventory.registerAtt("generic.projectile_piercing", true, () ->
                        new RangedAttribute("miapi.attribute.name.projectile_piercing", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> PROJECTILE_PIERCING = att);

        RegistryInventory.registerAtt("generic.elytra_turn_efficiency", true, () ->
                        new RangedAttribute("miapi.attribute.name.elytra_turn_efficiency", 0.0, -1024.0, 100.0).setSyncable(true),
                att -> ELYTRA_TURN_EFFICIENCY = att);
        RegistryInventory.registerAtt("generic.elytra_glide_efficiency", true, () ->
                        new RangedAttribute("miapi.attribute.name.elytra_glide_efficiency", 0.0, -1024.0, 100.0).setSyncable(true),
                att -> ELYTRA_GLIDE_EFFICIENCY = att);
        RegistryInventory.registerAtt("generic.elytra_rocket_efficiency", true, () ->
                        new RangedAttribute("miapi.attribute.name.elytra_rocket_efficiency", 1.0, 0.0, 1024.0).setSyncable(true),
                att -> ELYTRA_ROCKET_EFFICIENCY = att);
        RegistryInventory.registerAtt("generic.shielding_armor", true, () ->
                        new RangedAttribute("miapi.attribute.name.shielding_armor", 0.0, 0.0, 1024.0).setSyncable(true),
                att -> SHIELDING_ARMOR = att);
    }
}
