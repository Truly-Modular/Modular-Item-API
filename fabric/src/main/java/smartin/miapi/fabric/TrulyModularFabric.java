package smartin.miapi.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.registries.RegistryInventory;

import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;

public class TrulyModularFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        //DATA
        if(Environment.isClient()){
            MiapiClient.setupClient();
        }

        //ATTRIBUTE REPLACEMENT
        AttributeRegistry.ATTACK_RANGE = ReachEntityAttributes.ATTACK_RANGE;
        AttributeRegistry.REACH = ReachEntityAttributes.REACH;
        RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.swim_speed", 1.0, 0.0, 1024.0).setTracked(true),
                att -> SWIM_SPEED = att);

        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED);
        AttributeProperty.replaceMap.put("miapi:generic.reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("forge:block_reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", () -> AttributeRegistry.ATTACK_RANGE);
    }
}