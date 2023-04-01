package smartin.miapi;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;

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
        if(MinecraftClient.getInstance()==null){
            return false;
        }
        if(Miapi.server==null){
            return false;
        }
        return true;
    }
}
