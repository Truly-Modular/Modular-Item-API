package smartin.miapi.modules.properties.compat.better_combat;

/*
public class BetterCombatProperty implements ModuleProperty {
    public static String KEY = "better_combat_config";
    public static BetterCombatProperty property;

    public BetterCombatProperty() {
        property = this;
        if (Platform.isModLoaded("bettercombat")) {
            BetterCombatHelper.setup();
            AttributeProperty.attributeTransformers.add((map, itemstack) -> {
                JsonElement element = ItemModule.getMergedProperty(itemstack, property);
                if (element != null) {
                    map.removeAll(AttributeRegistry.ATTACK_RANGE);
                }
                return map;
            });
            CraftAction.events.add(new CraftAction.CraftingEvent() {
                @Override
                public ItemStack onCraft(ItemStack old, ItemStack crafted, @Nullable ModuleInstance crafting) {
                    BetterCombatHelper.applyNBT(crafted);
                    return crafted;
                }

                @Override
                public ItemStack onPreview(ItemStack old, ItemStack crafted, @Nullable ModuleInstance crafting) {
                    BetterCombatHelper.applyNBT(crafted);
                    return crafted;
                }
            });
            ModularItemStackConverter.converters.add(stack -> {
                BetterCombatHelper.applyNBT(stack);
                return stack;
            });
        }
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return Platform.isModLoaded("bettercombat");
    }

    @Override
    public JsonElement merge(JsonElement old, JsonElement toMerge, MergeType type) {
        if (type == MergeType.SMART || type == MergeType.EXTEND) {
            return toMerge;
        } else if (type == MergeType.OVERWRITE) {
            return old;
        }
        return ModuleProperty.super.merge(old, toMerge, type);
    }
}


 */