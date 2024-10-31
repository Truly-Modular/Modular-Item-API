package smartin.miapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.util.Identifier;
import smartin.miapi.client.model.item.ItemBakedModelReplacement;
import smartin.miapi.registries.RegistryInventory;

import java.util.List;

@Environment(EnvType.CLIENT)
public class MiapiClientFabric {

    public static void setupClient() {
        smartin.miapi.client.MiapiClient.KEY_BINDINGS.addCallback((keyBinding -> {
            KeyBindingHelper.registerKeyBinding(keyBinding);
        }));
        RegistryInventory.modularItems.addCallback((modularItem) -> {
            ArmorRenderer.register(new ModularArmorRenderer(), modularItem);
        });
        ModelLoadingPlugin.register(pluginContext -> {
            List<Identifier> ids = RegistryInventory.modularItems.getFlatMap().keySet().stream().map(string -> new Identifier(string.replace("item/", ""))).toList();
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
