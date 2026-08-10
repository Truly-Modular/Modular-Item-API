package smartin.miapi.modules.abilities;


import com.mojang.serialization.Codec;
import com.redpxnda.nucleus.codec.auto.AutoCodec;
import com.redpxnda.nucleus.codec.behavior.CodecBehavior;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.modules.properties.util.DoubleOperationResolvable;
import smartin.miapi.modules.properties.util.MergeAble;
import smartin.miapi.modules.properties.util.MergeType;

public class ThrowingAbilityContext {
    public static final Codec<ThrowingAbilityContext> CODEC = AutoCodec.of(ThrowingAbilityContext.class).codec();
    @AutoCodec.Name("min_hold_time")
    @CodecBehavior.Optional
    public DoubleOperationResolvable minUseTime = new DoubleOperationResolvable(10);

    @CodecBehavior.Optional
    public DoubleOperationResolvable cooldown = new DoubleOperationResolvable(0);

    @AutoCodec.Name("auto_release")
    @CodecBehavior.Optional
    @Nullable
    public Boolean autoRelease = null;

    @Nullable
    @CodecBehavior.Optional
    public String animation = null;

    @Nullable
    @CodecBehavior.Optional
    @AutoCodec.Name("pose_charge")
    public String poseCharge = null;

    @Nullable
    @CodecBehavior.Optional
    @AutoCodec.Name("pose_throw")
    public String poseThrow = null;

    @CodecBehavior.Optional
    @AutoCodec.Name("throw_position")
    public Transform throwPosition = Transform.IDENTITY;

    public ThrowingAbilityContext initialize(ModuleInstance moduleInstance) {
        ThrowingAbilityContext context = new ThrowingAbilityContext();
        context.autoRelease = autoRelease;
        context.animation = animation;
        context.minUseTime = minUseTime.initialize(moduleInstance);
        context.cooldown = cooldown.initialize(moduleInstance);
        context.poseCharge = poseCharge;
        context.poseThrow = poseThrow;
        context.throwPosition = throwPosition;
        return context;
    }

    public ThrowingAbilityContext merge(ThrowingAbilityContext other, MergeType mergeType) {
        ThrowingAbilityContext merged = new ThrowingAbilityContext();

        merged.minUseTime = minUseTime.merge(other.minUseTime, mergeType);
        merged.cooldown = cooldown.merge(other.cooldown, mergeType);

        merged.autoRelease = MergeAble.decideLeftRight(
                autoRelease,
                other.autoRelease,
                mergeType
        );

        merged.animation = MergeAble.decideLeftRight(
                animation,
                other.animation,
                mergeType
        );

        merged.poseCharge = MergeAble.decideLeftRight(
                poseCharge,
                other.poseCharge,
                mergeType
        );

        merged.throwPosition = Transform.merge(throwPosition, other.throwPosition);

        merged.poseThrow = MergeAble.decideLeftRight(
                poseThrow,
                other.poseThrow,
                mergeType
        );

        return merged;
    }
}