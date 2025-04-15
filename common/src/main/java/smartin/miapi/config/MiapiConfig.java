package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.config.ConfigBuilder;
import com.redpxnda.nucleus.config.ConfigManager;
import com.redpxnda.nucleus.config.ConfigObject;
import com.redpxnda.nucleus.config.ConfigType;
import net.fabricmc.api.EnvType;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.loot.LootHelper;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.GlintProperty;

import java.util.ArrayList;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiConfig {
    public static MiapiConfig INSTANCE = new MiapiConfig();
    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public MiapiClientConfig client;
    public MiapiServerConfig server;
    public static ConfigObject<MiapiServerConfig> serverConfigObject;
    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public static ConfigObject<MiapiClientConfig> clientConfigObject;

    public static MiapiClientConfig getClientConfig() {
        if (INSTANCE == null || INSTANCE.client == null) {
            return new MiapiClientConfig();
        }
        return INSTANCE.client;
    }

    public static MiapiServerConfig getServerConfig() {
        if (INSTANCE == null || INSTANCE.server == null) {
            return new MiapiServerConfig();
        }
        return INSTANCE.server;
    }


    public static void setupConfigs() {
        if (Environment.isClient()) {
            setupClientConfig();
        }
        MiapiConfig.serverConfigObject = ConfigManager.register(ConfigBuilder.automatic(MiapiServerConfig.class)
                .id(Miapi.MOD_ID + ":server")
                .fileLocation(Miapi.MOD_ID + "_server")
                .type(ConfigType.SERVER_CLIENT_SYNCED)
                .creator(MiapiServerConfig::new)
                .updateListener(c -> {
                    MiapiServerConfig.INSTANCE = c;
                    INSTANCE.server = c;
                    ModularItemCache.discardCache();
                    if (Miapi.server != null && Miapi.server.getConnection() != null) {
                        //CacheCommands.clearCacheAllClients(Miapi.server);
                    }
                    LootHelper.adjusted = new ArrayList<>();
                    if (MiapiConfig.getServerConfig().lootCategory.isEnabled) {
                        if (MiapiConfig.getServerConfig().lootCategory.isSwappingMaterials) {
                            LootHelper.adjusted.add(MiapiConfig.getServerConfig().lootCategory.materialSwapLootFunction);
                        }
                        if (MiapiConfig.getServerConfig().lootCategory.isSwappingModules) {
                            LootHelper.adjusted.add(MiapiConfig.getServerConfig().lootCategory.moduleSwapLootFunction);
                        }
                    }
                }));
        serverConfigObject.load();
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public static void setupClientConfig() {
        MiapiConfig.clientConfigObject = ConfigManager.register(ConfigBuilder.automatic(MiapiClientConfig.class)
                .id(Miapi.MOD_ID + ":client")
                .fileLocation(Miapi.MOD_ID + "_client")
                .type(ConfigType.COMMON)
                .creator(MiapiClientConfig::new)
                .updateListener(c -> {
                    MiapiClientConfig.INSTANCE = c;
                    INSTANCE.client = c;
                    if (Environment.isClient()) {
                        GlintProperty.updateConfig();
                    }
                    KeyBindManager.configLoad(MiapiConfig.getClientConfig().other.bindings);
                    ModularItemCache.discardCache();
                }));
    }
}
