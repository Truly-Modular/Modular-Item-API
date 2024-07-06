package smartin.miapi.modules.properties.enchanment;

/*
public class FakeEnchantmentProperty implements ModuleProperty {
    public static FakeEnchantmentProperty property;
    public static final String KEY = "fake_enchant";
    private static final Type type = new TypeToken<Map<String, Integer>>() {
    }.getType();

    public FakeEnchantmentProperty() {
        property = this;
        ModularItemCache.setSupplier(KEY, FakeEnchantmentProperty::getEnchantsCache);
        FakeEnchantment.enchantmentTransformers.add((enchantment, stack, level) -> {
            if (getEnchants(stack).containsKey(enchantment)) {
                return Math.max(getEnchants(stack).get(enchantment), level);
            }
            return level;
        });
        if (Environment.isClient()) {
            setupClient();
        }
    }

    @net.fabricmc.api.Environment(EnvType.CLIENT)
    public void setupClient() {
        StatListWidget.addStatDisplaySupplier(new StatListWidget.StatWidgetSupplier() {
            @Override
            public <T extends InteractAbleWidget & SingleStatDisplay> List<T> currentList(ItemStack original, ItemStack compareTo) {
                List<T> displays = new ArrayList<>();

                Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchants(original));
                getEnchants(original).forEach((enchantment, integer) -> {
                    if (enchantments.containsKey(enchantment)) {
                        enchantments.put(enchantment, Math.max(integer, enchantments.get(enchantment)));
                    }
                    enchantments.put(enchantment, integer);
                });
                enchantments.keySet().forEach(enchantment -> {
                    JsonStatDisplay display = new JsonStatDisplay((stack) -> Component.translatable(enchantment.getTranslationKey()),
                            (stack) -> Component.translatable(enchantment.getTranslationKey()),
                            new SingleStatDisplayDouble.StatReaderHelper() {
                                @Override
                                public double getValue(ItemStack itemStack) {
                                    return EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemStack);
                                }

                                @Override
                                public boolean hasValue(ItemStack itemStack) {
                                    return true;
                                }
                            },
                            0,
                            enchantment.getMaxLevel());
                    if(enchantment.isCursed()){
                        display.inverse = true;
                    }
                    displays.add((T) display);
                });
                return displays;
            }
        });
    }

    private static Map<Enchantment, Integer> getEnchantsCache(ItemStack itemStack) {
        Map<Enchantment, Integer> enchants = new HashMap<>();

        JsonElement list = ItemModule.getMergedProperty(itemStack, property, MergeType.SMART);
        ItemModule.getMergedProperty(ItemModule.getModules(itemStack), property);
        Map<String, Integer> map = Miapi.gson.decode(list, type);
        if (map != null) {
            map.forEach((id, level) -> {
                Enchantment enchantment = Registries.ENCHANTMENT.get(new ResourceLocation(id));
                if (enchantment != null && enchantment.canEnchant(itemStack)) {
                    enchants.put(enchantment, level);
                }
            });
        }
        return enchants;
    }

    public static Map<Enchantment, Integer> getEnchants(ItemStack itemStack) {
        return ModularItemCache.get(itemStack, KEY, new HashMap<>());
    }

    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType mergeType) {
        if (old != null && toMerge != null) {
            Map<String, Integer> mapOld = Miapi.gson.decode(old, type);
            Map<String, Integer> mapToMerge = Miapi.gson.decode(toMerge, type);
            if (mergeType.equals(MergeType.OVERWRITE)) {
                return toMerge;
            }
            mapOld.forEach((key, level) -> {
                if (mapToMerge.containsKey(key)) {
                    mapToMerge.put(key, Math.max(mapOld.get(key), mapToMerge.get(key)));
                } else {
                    mapToMerge.put(key, level);
                }
            });
            return Miapi.gson.toJsonTree(mapToMerge);
        }
        if (old == null && toMerge != null) {
            return toMerge;
        }
        return old;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        Miapi.gson.decode(data, type);
        return true;
    }
}


     */