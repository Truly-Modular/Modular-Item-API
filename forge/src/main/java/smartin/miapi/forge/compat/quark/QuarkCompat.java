package smartin.miapi.forge.compat.quark;

import smartin.miapi.Environment;

public class QuarkCompat {

    public static void setup() {
        if (Environment.isClient()) {
            smartin.miapi.forge.compat.quark.QuarkClientCompat.setup();
        }
    }
}
