package smartin.miapi.fabric;

import ht.treechop.api.ITreeChopAPIProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

public class TrechopUtilFabric {
    public static void loadTreechopCompat(){
        FabricLoader.getInstance().getObjectShare().whenAvailable("treechop:api_provider", (key, value) -> {
            if (value instanceof ITreeChopAPIProvider provider) {
                //TreechopUtil.api = provider.get(Miapi.MOD_ID);
                Player player;
            }
        });
    }
}
