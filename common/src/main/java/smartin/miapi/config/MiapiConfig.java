package smartin.miapi.config;

import dev.architectury.platform.Platform;

public class MiapiConfig {

    public static boolean getBetterInfinity(){
        return true;
    }
    public static boolean getBetterLoyalty(){
        return true;
    }
    public static boolean isDevelopment(){
        return Platform.isDevelopmentEnvironment();
    }

}
