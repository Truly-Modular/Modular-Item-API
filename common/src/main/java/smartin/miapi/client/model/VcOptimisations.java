package smartin.miapi.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.architectury.platform.Platform;

public class VcOptimisations {
    public static boolean IMMEDIATLY_FAST_LOADED = Platform.isModLoaded("immediatly_fast");

    public static VertexConsumer optimize(VertexConsumer consumer) {
        if(IMMEDIATLY_FAST_LOADED){
        }
        return consumer;
    }
}
