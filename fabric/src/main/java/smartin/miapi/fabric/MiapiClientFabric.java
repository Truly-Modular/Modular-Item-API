package smartin.miapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MiapiClientFabric {

    public static void setupClient() {
        //smartin.miapi.client.MiapiClient.KEY_BINDINGS.addCallback((KeyBindingHelper::registerKeyBinding));
        RegistryInventory.modularItems.addCallback((item) -> ArmorRenderer.register(new ModularArmorRenderer(), item));
        ModelLoadingPlugin.register(pluginContext -> {
            List<ResourceLocation> ids = RegistryInventory.modularItems.getFlatMap().keySet().stream().map(id -> ResourceLocation.parse(id.toString().replace("item/", ""))).toList();
            pluginContext.addModels(ids);
            pluginContext.resolveModel().register((context) -> {
                if (ItemBakedModelReplacement.isModularItem(context.id())) {
                    return new ItemBakedModelReplacement();
                } else {
                    return null;
                }
            });
        });
    }
}
