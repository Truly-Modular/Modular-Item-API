package smartin.miapi.forge.compat.minecolonies;

public class MineColoniesCompat {
    public static void setup() {
        com.minecolonies.api.compatibility.Compatibility.tinkersCompat = new TrulyModularToolHelper(com.minecolonies.api.compatibility.Compatibility.tinkersCompat);
    }
}
