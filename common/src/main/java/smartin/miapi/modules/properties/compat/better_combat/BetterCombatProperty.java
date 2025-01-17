package smartin.miapi.modules.properties.compat.better_combat;

import net.bettercombat.api.component.BetterCombatDataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class BetterCombatProperty extends CodecProperty<ResourceLocation> implements ComponentApplyProperty {
    public static BetterCombatProperty property;

    public BetterCombatProperty() {
        super(ResourceLocation.CODEC);
        property = this;
    }

    @Override
    public ResourceLocation merge(ResourceLocation left, ResourceLocation right, MergeType mergeType) {
        return MergeAble.decideLeftRight(left, right, mergeType);
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {
        var optional = getData(itemStack);
        optional.ifPresent(resourceLocation -> itemStack.set(BetterCombatDataComponents.WEAPON_PRESET_ID, resourceLocation));
    }
}