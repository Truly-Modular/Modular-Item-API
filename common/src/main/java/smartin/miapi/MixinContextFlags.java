package smartin.miapi;

public class MixinContextFlags {
    public static final ThreadLocal<Boolean> CALLED_FROM_MUTABLE = ThreadLocal.withInitial(() -> false);
}

