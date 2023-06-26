package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.client.ModelPredicateProviderRegistryAccessor;

@Environment(EnvType.CLIENT)
public class ModularModelPredicateProvider {
    private ModularModelPredicateProvider(){

    }

    public static void registerModularModelOverride(Identifier id, ClampedModelPredicateProvider provider){
        Miapi.itemRegistry.addCallback((item)->{
            ModelPredicateProviderRegistryAccessor.register(item,id,provider);
        });
    }

    public static void registerModelOverride(Item item, Identifier id, ClampedModelPredicateProvider provider){
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }

    public static void registerModularItemModelOverride(Identifier identifier, Identifier id, ClampedModelPredicateProvider provider){
        Item item = Miapi.itemRegistry.get(identifier.toString());
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }
}
