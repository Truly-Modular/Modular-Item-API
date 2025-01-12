package smartin.miapi.material;

import com.mojang.serialization.Codec;
import dev.architectury.event.EventResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.StackStorageComponent;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

//DIsabled for now, not sure if this makes sense with component logic to have
public class MaterialInscribeProperty extends CodecProperty<String> {
    public static final String KEY = "inscribe_on_craft";
    public static MaterialInscribeProperty property;

    public MaterialInscribeProperty() {
        super(Codec.STRING);
        property = this;
        MiapiEvents.MATERIAL_CRAFT_EVENT.register((listener) -> {
            listener.crafted = inscribe(listener.crafted, listener.materialStack);
            return EventResult.pass();
        });
    }

    public static ItemStack inscribe(ItemStack raw, ItemStack materialStack) {
        Optional<String> optional = property.getData(raw);
        optional.ifPresent(s -> raw.update(StackStorageComponent.STACK_STORAGE_COMPONENT, Map.of(), old -> {
            Map<String, ItemStack> map = new HashMap<>(old);
            map.put(s, materialStack);
            if (s.equals("POTION")) {
                PotionContents contents = materialStack.get(DataComponents.POTION_CONTENTS);
                if (contents != null) {
                    raw.set(DataComponents.POTION_CONTENTS, contents);
                }
            }
            return map;
        }));
        return raw;
    }

    @Override
    public String merge(String left, String right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }
}
