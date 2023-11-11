package smartin.miapi.blueprint;

import com.redpxnda.nucleus.facet.Facet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.HashMap;
import java.util.Map;

public class PlayerBoundBlueprint implements Facet<NbtElement> {
    public Map<String, Object> map = new HashMap<>();

    @Override
    public NbtElement toNbt() {
        NbtCompound tag = new NbtCompound();
        //tag.put()
        NbtCompound element = new NbtCompound();
        for (String key : map.keySet()) {
            element.putString(key, "asd");
        }
        tag.put("blueprints", element);
        return tag;
    }

    @Override
    public void loadNbt(NbtElement nbtElement) {

    }
}
