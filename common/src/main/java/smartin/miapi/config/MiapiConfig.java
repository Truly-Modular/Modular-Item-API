package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;
import com.redpxnda.nucleus.config.ConfigBuilder;
import com.redpxnda.nucleus.config.ConfigManager;
import com.redpxnda.nucleus.config.ConfigObject;
import com.redpxnda.nucleus.config.ConfigType;
import smartin.miapi.Environment;
import smartin.miapi.Miapi;
import smartin.miapi.modules.abilities.key.KeyBindManager;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.GlintProperty;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiConfig {
    public static MiapiConfig INSTANCE = new MiapiConfig();
    public MiapiClientConfig client;
    public MiapiServerConfig server;
    public static ConfigObject<MiapiServerConfig> serverConfigObject;
    public static ConfigObject<MiapiClientConfig> clientConfigObject;


    public static void setupConfigs() {
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
                    KeyBindManager.configLoad(MiapiConfig.INSTANCE.client.other.bindings);
                    ModularItemCache.discardCache();
                }));
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
                }));
        serverConfigObject.load();
    }
}
