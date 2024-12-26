package smartin.miapi.config;

import com.redpxnda.nucleus.codec.auto.ConfigAutoCodec;

@ConfigAutoCodec.ConfigClassMarker
public class MiapiConfig {
    public static MiapiConfig INSTANCE = new MiapiConfig();

    public MiapiClientConfig client = MiapiClientConfig.INSTANCE;
    public MiapiServerConfig server = MiapiServerConfig.INSTANCE;
}
