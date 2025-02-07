package smartin.miapi.material;

import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.CodecProperty;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaterialStatIndicatorProperty extends CodecProperty<Map<String, MaterialStatIndicatorProperty.Context>> {
    public static Codec<Context> INNER_CODEC = Codec.withAlternative(AutoCodec.of(Context.class).codec(),
            Codec.INT.xmap(i -> {
                Context context = new Context();
                context.strength = i;
                return context;
            }, c -> c.strength));
    public static Codec<Map<String, Context>> CODEC = Codec.unboundedMap(Codec.STRING, INNER_CODEC);
    public static ResourceLocation KEY = Miapi.id("material_indication");
    public static MaterialStatIndicatorProperty property;

    public MaterialStatIndicatorProperty() {
        super(CODEC);
        property = this;
    }

    @Override
    public Map<String, Context> initialize(Map<String, Context> map, ModuleInstance moduleInstance) {
        return initialize(map);
    }

    public Map<String, Context> initialize(Map<String, Context> map) {
        Map<String, Context> init = new LinkedHashMap<>();
        map.forEach((id, data) -> {
            Context initContext = new Context();
            initContext.strength = data.strength;
            initContext.info = data.info;
            if (data.info == null) {
                if (id.equals("durability")) {
                    initContext.info = Component.translatable("miapi.material_indication.default.durability");
                } else if (id.equals("hardness")) {
                    initContext.info = Component.translatable("miapi.material_indication.default.hardness");
                }
            }
            init.put(id, initContext);
        });
        return init;
    }


    @Override
    public Map<String, Context> merge(Map<String, Context> left, Map<String, Context> right, MergeType mergeType) {
        return MergeAble.mergeMap(left, right, mergeType);
    }

    public static class Context {
        public Integer strength = 0;
        @CodecBehavior.Optional
        public Component info = null;
    }
}
