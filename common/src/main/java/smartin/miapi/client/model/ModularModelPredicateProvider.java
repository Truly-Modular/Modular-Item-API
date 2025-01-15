package smartin.miapi.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import smartin.miapi.mixin.client.ModelPredicateProviderRegistryAccessor;
import smartin.miapi.registries.RegistryInventory;

@Environment(EnvType.CLIENT)
public class ModularModelPredicateProvider {
    private ModularModelPredicateProvider() {

    }

    public static void registerModularModelOverride(Identifier id, ClampedModelPredicateProvider provider) {
        RegistryInventory.addCallback(RegistryInventory.modularItems, item -> {
            ModelPredicateProviderRegistryAccessor.register(item, id, provider);
        });
    }

    public static void registerModelOverride(Item item, Identifier id, ClampedModelPredicateProvider provider) {
        ModelPredicateProviderRegistryAccessor.register(item, id, provider);
    }

    public static void registerModularItemModelOverride(Identifier identifier, Identifier id, ClampedModelPredicateProvider provider) {
        Item item = RegistryInventory.modularItems.get(identifier);
        ModelPredicateProviderRegistryAccessor.register(item, id, provider);
    }
}
