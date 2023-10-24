package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.crafting.statdisplay.SingleStatDisplayDouble;
import smartin.miapi.client.gui.crafting.statdisplay.StatListWidget;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.GuiWidgetSupplier;
import smartin.miapi.modules.properties.util.MergeType;
import smartin.miapi.modules.properties.util.ModuleProperty;

/**
 * This property controls {@link smartin.miapi.modules.abilities.HeavyAttackAbility}
 */
public class HeavyAttackProperty implements ModuleProperty, GuiWidgetSupplier {
    public static String KEY = "heavyAttack";
    public static HeavyAttackProperty property;

    public HeavyAttackProperty() {
        property = this;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        new HeavyAttackJson(data, new ItemModule.ModuleInstance(ItemModule.empty));
        return true;
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case EXTEND, SMART -> {
                return old.deepCopy();
            }
        }
        return old.deepCopy();
    }

    public HeavyAttackJson get(ItemStack itemStack) {
        JsonElement jsonElement = ItemModule.getMergedProperty(itemStack, property);
        ItemModule.ModuleInstance instance = ItemModule.getModules(itemStack);
        for (ItemModule.ModuleInstance moduleInstance : instance.allSubModules()) {
            if (moduleInstance.getProperties().containsKey(property)) {
                instance = moduleInstance;
                jsonElement = moduleInstance.getProperties().get(property);
            }
        }
        if (jsonElement == null) {
            return null;
        }

        return new HeavyAttackJson(jsonElement, instance);
    }

    public boolean hasHeavyAttack(ItemStack itemStack) {
        return get(itemStack) != null;
    }

    @Override
    public StatListWidget.TextGetter getTitle() {
        return (stack -> {
            HeavyAttackJson json = get(stack);
            if (json != null) {
                Text asd = Text.translatable(json.title);
                return asd;
            }
            return Text.empty();
        });
    }

    @Override
    public StatListWidget.TextGetter getDescription() {
        return (stack -> {
            HeavyAttackJson json = get(stack);
            if (json != null) {
                Text asd = Text.translatable(json.description, json.damage, json.range, json.minHold / 20);
                return asd;
            }
            return Text.empty();
        });
    }

    @Override
    public SingleStatDisplayDouble.StatReaderHelper getStatReader() {
        return new SingleStatDisplayDouble.StatReaderHelper() {
            @Override
            public double getValue(ItemStack itemStack) {
                HeavyAttackJson json = get(itemStack);
                if (json != null) {
                    return json.range;
                }
                return 0;
            }

            @Override
            public boolean hasValue(ItemStack itemStack) {
                return get(itemStack) != null;
            }
        };
    }

    public static class HeavyAttackJson {
        public double damage;
        public double sweeping;
        public double range;
        public double minHold;
        public double cooldown;
        public String title = "miapi.ability.heavy_attack.title";
        public String description = "miapi.ability.heavy_attack.description";

        public HeavyAttackJson(JsonElement element, ItemModule.ModuleInstance instance) {
            JsonObject object = element.getAsJsonObject();
            damage = get(object.get("damage"), instance);
            sweeping = get(object.get("sweeping"), instance);
            range = get(object.get("range"), instance);
            minHold = get(object.get("minHold"), instance);
            cooldown = get(object.get("cooldown"), instance);
            if (object.has("title")) {
                title = getString(object.get("title"), instance);
            }
            if (object.has("description")) {
                description = getString(object.get("description"), instance);
            }
        }

        private double get(JsonElement object, ItemModule.ModuleInstance instance) {
            try {
                return object.getAsDouble();
            } catch (Exception e) {
                return StatResolver.resolveDouble(object.getAsString(), instance);
            }
        }

        private String getString(JsonElement object, ItemModule.ModuleInstance instance) {
            try {
                return object.getAsString();
            } catch (Exception e) {
                return StatResolver.resolveString(object.getAsString(), instance);
            }
        }

    }
}
