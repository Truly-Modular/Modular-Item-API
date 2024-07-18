package smartin.miapi.modules.properties.compat.better_combat;

/*
public class BetterCombatHelper {
    public static void setup() {
        ModularItemCache.setSupplier(BetterCombatProperty.KEY, itemStack -> new AttributeHolder(BetterCombatHelper.getAttributesContainer(itemStack)));
    }

    private static float getAttackRange(ItemStack stack) {
        if (Platform.isForgeLike()) {
            return (float) (AttributeUtil.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, AttributeRegistry.ATTACK_RANGE.getDefaultValue()) - 0.5f);
        }
        return (float) (AttributeUtil.getActualValueFrom(AttributeProperty.getAttributeModifiersRaw(stack), EquipmentSlot.MAINHAND, AttributeRegistry.ATTACK_RANGE, AttributeRegistry.ATTACK_RANGE.getDefaultValue()) + 2.5f);
    }

    public static net.bettercombat.api.WeaponAttributes getAttributesContainer(ItemStack stack) {
        if (stack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes attributes = container(ItemModule.getMergedProperty(stack, BetterCombatProperty.property));
            if (attributes != null) {
                attributes = new net.bettercombat.api.WeaponAttributes((attributes.attackRange() + getAttackRange(stack)) / 2, attributes.pose(), attributes.offHandPose(), attributes.isTwoHanded(), attributes.category(), attributes.attacks());
            }
            return attributes;
        } else {
            return null;
        }
    }

    private static net.bettercombat.api.WeaponAttributes container(JsonElement data) {
        if (data == null) {
            return null;
        }
        String jsonString = data.toString();
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        return net.bettercombat.logic.WeaponRegistry.resolveAttributes(ResourceLocation.fromNamespaceAndPath(Miapi.MOD_ID, "modular_item"), WeaponAttributesHelper.decode(jsonReader));
    }

    public static ItemStack applyNBT(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ModularItem) {
            net.bettercombat.api.WeaponAttributes container = getAttributesContainer(itemStack);
            //TODO: rework this once better combat is available for 1.21
            if (container != null) {
                WeaponAttributesHelper.writeToNBT(itemStack, new AttributesContainer(null, container));
            } else {
            }
        }
        return itemStack;
    }

    @Nullable
    public static net.bettercombat.api.WeaponAttributes getAttributes(ItemStack stack) {
        return ModularItemCache.get(stack, BetterCombatProperty.KEY, new AttributeHolder(null)).attributes();
    }

    public record AttributeHolder(net.bettercombat.api.WeaponAttributes attributes) {
    }
}

 */
