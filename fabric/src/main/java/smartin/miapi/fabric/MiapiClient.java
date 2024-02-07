package smartin.miapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.util.Identifier;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MiapiClient {

    public static void setupClient() {
        ModelLoadingPlugin.register(pluginContext -> {
            List<Identifier> ids = RegistryInventory.modularItems.getFlatMap().keySet().stream().map(string -> new Identifier(string.replace("item/", ""))).toList();
            pluginContext.addModels(ids);
            pluginContext.resolveModel().register((context) -> {
                context.id();
                if (ItemBakedModelReplacement.isModularItem(context.id())) {
                    return new ItemBakedModelReplacement();
                } else {
                    return null;
                }
            });
        });
    }
}
