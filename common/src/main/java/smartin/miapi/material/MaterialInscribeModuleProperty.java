package smartin.miapi.material;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.architectury.event.EventResult;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MaterialInscribeModuleProperty extends CodecProperty<String> {
    public static final String KEY = "inscribe_on_craft_module";
    public static MaterialInscribeModuleProperty property;
    public static Codec<Map<String, ItemStack>> CODEC = Codec.unboundedMap(Codec.STRING, ItemStack.CODEC);

    public MaterialInscribeModuleProperty() {
        super(Codec.STRING);
        property = this;
        MiapiEvents.MATERIAL_CRAFT_EVENT.register((listener) -> {
            if (listener.crafted != null && listener.moduleInstance != null) {
                inscribe(listener.moduleInstance, listener.crafted, listener.materialStack);
            }
            return EventResult.pass();
        });
    }

    public static void inscribe(ModuleInstance moduleInstance, ItemStack raw, ItemStack materialStack) {
        Optional<String> optional = property.getData(moduleInstance);
        optional.ifPresent((s) -> {
            JsonElement data = moduleInstance.moduleData.get(Miapi.id(KEY));
            Map<String, ItemStack> dataMap = new HashMap<>();
            if (data != null) {
                dataMap = CODEC.decode(JsonOps.INSTANCE, data).result().map(Pair::getFirst).orElse(new HashMap<>());
            }
            dataMap.put(s, materialStack);
            moduleInstance.moduleData.put(Miapi.id(KEY), CODEC.encodeStart(JsonOps.INSTANCE, dataMap).getOrThrow());
            moduleInstance.getRoot().writeToItem(raw);
        });
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
