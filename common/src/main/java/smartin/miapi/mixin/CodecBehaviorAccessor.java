package smartin.miapi.mixin;

import com.redpxnda.nucleus.codec.behavior.BiBehaviorOutline;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CodecBehavior.class, remap = false)
public interface CodecBehaviorAccessor {
    @Accessor
    static BiBehaviorOutline<CodecBehavior.Getter<?>> getGetters() {
        throw new UnsupportedOperationException();
    }
}
