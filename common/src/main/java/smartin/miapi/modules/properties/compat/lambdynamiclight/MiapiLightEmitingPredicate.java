package smartin.miapi.modules.properties.compat.lambdynamiclight;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.render.EmissivityProperty;

public class MiapiLightEmitingPredicate implements ItemSubPredicate {
    public static ItemPredicate predicate;
    public static Codec<MiapiLightEmitingPredicate> CODEC = AutoCodec.of(MiapiLightEmitingPredicate.class).codec();
    public static Type<MiapiLightEmitingPredicate> TYPE = new Type<>(CODEC);
    public int light;
    @CodecBehavior.Optional
    @AutoCodec.Name("water_sensitive")
    public boolean underWater = false;

    @Override
    public boolean matches(ItemStack stack) {
        if (ModularItem.isModularItem(stack)) {
            int lvl = 0;
            for (ModuleInstance m : ItemModule.getModules(stack).getFlatList()) {
                lvl = Math.max(lvl, EmissivityProperty.getLightValues(m)[1]);
            }
            return light == lvl;
        }
        return false;
    }
}
