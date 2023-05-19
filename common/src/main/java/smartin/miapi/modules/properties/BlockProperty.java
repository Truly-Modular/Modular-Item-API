package smartin.miapi.modules.properties;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.attributes.AttributeRegistry;
import smartin.miapi.item.modular.EntityAttributeAbility;
import smartin.miapi.item.modular.ItemAbilityManager;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.modules.properties.util.SimpleDoubleProperty;

public class BlockProperty extends SimpleDoubleProperty implements ModuleProperty {
    public static final String KEY = "blocking";

    public BlockProperty() {
        super(KEY);
        new BlockAbility();
    }

    public class BlockAbility extends EntityAttributeAbility {

        public BlockAbility() {
            ItemAbilityManager.useAbilityRegistry.register("block", this);
        }

        @Override
        protected Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack itemStack) {
            Miapi.LOGGER.warn("getAttributes");
            Multimap<EntityAttribute, EntityAttributeModifier> multimap = ArrayListMultimap.create();
            //TODO:make this value relate to the Actual Property
            double value = 0.8;
            multimap.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier("test", -value, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            multimap.put(AttributeRegistry.DAMAGE_RESISTANCE, new EntityAttributeModifier("test", value, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            return multimap;
        }

        @Override
        public boolean allowedOnItem(ItemStack itemStack) {
            Miapi.LOGGER.warn("allowedCheck");
            return true;
        }

        @Override
        public int getMaxUseTime(ItemStack itemStack) {
            return 20 * 60 * 60;
        }
    }
}
