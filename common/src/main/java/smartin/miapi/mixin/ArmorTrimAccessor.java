package smartin.miapi.mixin;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ArmorTrim.class)
public interface ArmorTrimAccessor {
    @Invoker
    String callGetMaterialAssetNameFor(ArmorMaterial armorMaterial);
}
