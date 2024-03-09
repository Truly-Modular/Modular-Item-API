package smartin.miapi.fabric;

import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import dev.architectury.platform.Platform;
import ht.treechop.api.ITreeChopAPIProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.ModifyItemAttributeModifiersCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.AttributeProperty;
import smartin.miapi.modules.properties.compat.ht_treechop.TreechopUtil;
import smartin.miapi.registries.RegistryInventory;

import static smartin.miapi.attributes.AttributeRegistry.SWIM_SPEED;
import static smartin.miapi.events.MiapiEvents.ITEM_STACK_ATTRIBUTE_EVENT;

public class MiapiFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Miapi.init();

        //DATA
        if (Environment.isClient()) {
            MiapiClientFabric.setupClient();
        }

        ModifyItemAttributeModifiersCallback.EVENT.register((stack, slot, attributeModifiers) -> {
            if (stack.getItem() instanceof ModularItem) {
                ITEM_STACK_ATTRIBUTE_EVENT.invoker().adjust(new MiapiEvents.ItemStackAttributeEventHolder(stack,slot,attributeModifiers));
            }
        });

        //ATTRIBUTE REPLACEMENT
        AttributeRegistry.ATTACK_RANGE = ReachEntityAttributes.ATTACK_RANGE;
        AttributeRegistry.REACH = ReachEntityAttributes.REACH;
        RegistryInventory.registerAtt("generic.swim_speed", true, () ->
                        new ClampedEntityAttribute("miapi.attribute.name.swim_speed", 1.0, 0.0, 1024.0).setTracked(true),
                att -> SWIM_SPEED = att);

        if(Platform.isModLoaded("treechop")){
            FabricLoader.getInstance().getObjectShare().whenAvailable("treechop:api_provider", (key, value) -> {
                if (value instanceof ITreeChopAPIProvider provider) {
                    TreechopUtil.api = provider.get(Miapi.MOD_ID);
                }
            });
        }

        AttributeProperty.replaceMap.put("forge:generic.swim_speed", () -> SWIM_SPEED);
        AttributeProperty.replaceMap.put("miapi:generic.reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("miapi:generic.attack_range", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("forge:block_reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("forge:entity_reach", () -> AttributeRegistry.ATTACK_RANGE);
        AttributeProperty.replaceMap.put("reach-entity-attributes:reach", () -> AttributeRegistry.REACH);
        AttributeProperty.replaceMap.put("reach-entity-attributes:attack_range", () -> AttributeRegistry.ATTACK_RANGE);
    }
}