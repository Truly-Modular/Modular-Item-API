package smartin.miapi.forge.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemAttributeModifierEvent.class)
public interface ItemAttributeModifierEventAccessor {
    @Mutable
    @Accessor
    void setOriginalModifiers(Multimap<EntityAttribute, EntityAttributeModifier> originalModifiers);
}
