package smartin.miapi.fabric;

import ht.treechop.api.ITreeChopAPIProvider;
import net.fabricmc.loader.api.FabricLoader;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.compat.ht_treechop.TreechopUtil;

public class TrechopUtilFabric {
    public static void loadTreechopCompat(){
        FabricLoader.getInstance().getObjectShare().whenAvailable("treechop:api_provider", (key, value) -> {
            if (value instanceof ITreeChopAPIProvider provider) {
                TreechopUtil.api = provider.get(Miapi.MOD_ID);
            }
        });
    }
}
