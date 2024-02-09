package smartin.miapi.forge.compat.epic_fight;

import com.google.gson.JsonElement;
import dev.architectury.platform.Platform;
import smartin.miapi.modules.properties.util.ModuleProperty;

public class EpicFightCompatProperty implements ModuleProperty {
    public static EpicFightCompatProperty property;
    public static String KEY = "epic_fight";

    public EpicFightCompatProperty(){
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return Platform.isModLoaded("epicfight");
    }
}
