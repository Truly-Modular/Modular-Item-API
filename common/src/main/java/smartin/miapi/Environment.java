package smartin.miapi;

import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

/**
 * A Util class to help with client Server detection
 */
public class Environment {

    /**
     * This method returns if the current thread has access to Client classes
     * in most cases this will return true if a logical client is loaded
     */
    public static boolean isClient(){
        return Platform.getEnv() == EnvType.CLIENT;
    }
}
