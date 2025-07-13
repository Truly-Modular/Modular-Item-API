package smartin.miapi.forge.compat.epic_fight;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.modules.properties.attributes.AttributeProperty;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.ComponentApplyProperty;
import smartin.miapi.modules.properties.util.MergeType;
import yesman.epicfight.registry.entries.EpicFightDataComponentTypes;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

/**
 * This is not fully implemented, this should be reworked if indepth compat with EF is desired.
 */
public class EpicFightCompatProperty extends CodecProperty<EpicFightCompatProperty.EpicFightData> implements ComponentApplyProperty {
    public static EpicFightCompatProperty property;
    public static String KEY = "epic_fight";
    public static Codec<EpicFightData> CODEC = AutoCodec.of(EpicFightData.class).codec();

    public static Codec<Style> STYLE_CODEC = Codec.STRING.xmap(Style.ENUM_MANAGER::get, Object::toString);
    public static Codec<WeaponCategory> WEAPON_CATEGORY_CODEC = Codec.STRING.xmap(WeaponCategory.ENUM_MANAGER::get, Object::toString);

    public EpicFightCompatProperty() {
        super(CODEC);
        property = this;
        CapabilityItem.builder().build();
        Style.ENUM_MANAGER.get("");
        AttributeProperty property1;
    }

    @Override
    public void updateComponent(ItemStack itemStack, @Nullable RegistryAccess registryAccess) {
        EpicFightDataComponentTypes epicFightDataComponentTypes;
    }

    @Override
    public EpicFightData merge(EpicFightData left, EpicFightData right, MergeType mergeType) {
        return null;
    }

    public static record StyleEntry(Style style, Holder<Attribute> attribute, AttributeModifier modifier) {

    }

    public static record EpicFightData() {

    }
}
