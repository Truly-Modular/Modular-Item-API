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
            e.printStackTrace();
            return false;
        }
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    protected static void isClientPrivate(){
    }

    public static boolean isClientServer(){
        if(!isClient()){
            return false;
        }
        if(Miapi.server==null){
            return false;
        }
        return true;
    }
}
