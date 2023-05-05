package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.UnclampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import smartin.miapi.Miapi;
import smartin.miapi.mixin.ModelPredicateProviderRegistryAccessor;

@Environment(EnvType.CLIENT)
public class ModularModelPredicateProvider {
    private ModularModelPredicateProvider(){

    }

    public static void registerModularModelOverride(Identifier id, UnclampedModelPredicateProvider provider){
        Miapi.itemRegistry.addCallback((item)->{
            ModelPredicateProviderRegistryAccessor.register(item,id,provider);
        });
    }

    public static void registerModelOverride(Item item, Identifier id, UnclampedModelPredicateProvider provider){
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }

    public static void registerModularItemModelOverride(Identifier identifier, Identifier id, UnclampedModelPredicateProvider provider){
        Item item = Miapi.itemRegistry.get(identifier.toString());
        ModelPredicateProviderRegistryAccessor.register(item,id,provider);
    }
}
