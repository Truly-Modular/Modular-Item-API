package smartin.miapi;

import net.fabricmc.api.EnvType;

/**
 * A Util class to help with client Server detection
 */
public class Environment {

    public static boolean isClient(){
        try {
            Environment.class.getDeclaredMethod("isClientPrivate");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    protected static void isClientPrivate(){
        //This function exist to check if this is the client
    }

    public static boolean isClientServer(){
        if(!isClient()){
            return false;
        }
        return Miapi.server==null;
    }
}
