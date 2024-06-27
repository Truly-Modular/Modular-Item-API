package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import smartin.miapi.mixin.client.ModelPredicateProviderRegistryAccessor;
import smartin.miapi.registries.RegistryInventory;

@Environment(EnvType.CLIENT)
public class ModularModelPredicateProvider {
    private ModularModelPredicateProvider(){

    }

    public static void registerModularModelOverride(ResourceLocation id, ClampedItemPropertyFunction provider){
        RegistryInventory.addCallback(RegistryInventory.modularItems, item -> {
            ModelPredicateProviderRegistryAccessor.register(item,id,provider);
        });
    }

    public static void registerModelOverride(Item item, ResourceLocation id, ClampedItemPropertyFunction provider){
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }

    public static void registerModularItemModelOverride(ResourceLocation identifier, ResourceLocation id, ClampedItemPropertyFunction provider){
        Item item = RegistryInventory.modularItems.get(identifier);
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }
}
