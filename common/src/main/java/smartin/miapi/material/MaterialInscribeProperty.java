package smartin.miapi.material;

import dev.architectury.event.EventResult;
import smartin.miapi.events.MiapiEvents;
import smartin.miapi.modules.properties.util.ModuleProperty;

//DIsabled for now, not sure if this makes sense with component logic to have
public abstract class MaterialInscribeProperty implements ModuleProperty {
    public static final String KEY = "inscribe_on_craft";
    public static MaterialInscribeProperty property;

    public MaterialInscribeProperty() {
        property = this;
        MiapiEvents.MATERIAL_CRAFT_EVENT.register((listener) -> {
            //listener.crafted = inscribe(listener.crafted, listener.materialStack);
            return EventResult.pass();
        });
    }

    /*
    public static ItemStack inscribe(ItemStack raw, ItemStack materialStack) {
        JsonElement element = ItemModule.getMergedProperty(raw, property);
        if (element != null) {
            CompoundTag compound = raw.getOrCreateNbt();
            materialStack = materialStack.copy();
            materialStack.setCount(1);
            compound.put(element.getAsString(), materialStack.writeNbt(new CompoundTag()));
            if(materialStack.hasNbt()){
                compound.merge(materialStack.getOrCreateNbt());
            }
            raw.setNbt(compound);
        }
        return raw;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsString();
        return true;
    }

     */
}
