package smartin.miapi.modules.properties.compat.apoli;

/**
 * This property manages the active {@link smartin.miapi.modules.abilities.util.ItemUseAbility}
 */
/*
public class ApoliPowersProperty implements ModuleProperty {
    public static final String KEY = "apoli_powers";
    public static ApoliPowersProperty property;

    public ApoliPowersProperty() {
        property = this;
    }

    public static List<PowerJson> getPowerJson(ItemStack itemStack) {
        List<PowerJson> powers = new ArrayList<>();
        if (itemStack.getItem() instanceof ModularItem) {
            JsonElement json = ItemModule.getMergedProperty(itemStack, property);
            if (json != null) {
                json.getAsJsonArray().forEach(jsonElement -> {
                    powers.add(new PowerJson(jsonElement.getAsJsonObject()));
                });
            }
        }
        return powers;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        data.getAsJsonArray().forEach(jsonElement -> new PowerJson(jsonElement.getAsJsonObject()));
        return Platform.isModLoaded("apoli");
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        switch (type) {
            case OVERWRITE -> {
                return toMerge.deepCopy();
            }
            case SMART, EXTEND -> {
                JsonArray array = old.deepCopy().getAsJsonArray();
                array.addAll(toMerge.deepCopy().getAsJsonArray());
                return Miapi.gson.toJsonTree(array);
            }
        }
        return old;
    }

    public static class PowerJson {
        public EquipmentSlot slot;
        public ResourceLocation powerId;
        public boolean isHidden;
        public boolean isNegative;

        public PowerJson(JsonObject element) {
            slot = AttributeProperty.getSlot(element.get("slot").getAsString());
            powerId = new ResourceLocation(element.get("powerId").getAsString());
            isHidden = element.get("isHidden").getAsBoolean();
            isNegative = element.get("isNegative").getAsBoolean();
        }
    }
}

 */
