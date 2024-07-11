package smartin.miapi.modules.properties.util;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import smartin.miapi.modules.ModuleInstance;

/**
 * A Wrapper class to allow different Datatypes pre and Post ModuleInstance initialize
 *
 * @param <T> the raw Interpretation
 * @param <K> the fully initialized PropertyData
 */
public interface EitherModuleProperty<T, K> extends ModuleProperty<Either<T, K>> {

    K initializeDecode(T property, ModuleInstance context);

    K mergeInterpreted(K left, K right, MergeType mergeType);

    T mergeRaw(T left, T right, MergeType mergeType);

    T decodeRaw(JsonElement element);

    @Override
    default Either<T, K> decode(JsonElement element) {
        return Either.left(decodeRaw(element));
    }

    @Override
    default Either<T, K> initialize(Either<T, K> property, ModuleInstance context) {
        return Either.right(initializeDecode(property.left().get(),context));
    }

    @Override
    default Either<T, K> merge(Either<T, K> left, Either<T, K> right, MergeType mergeType) {
        if (left.left().isPresent()) {
            return Either.left(mergeRaw(left.left().get(), right.left().get(), mergeType));
        } else {
            return Either.right(mergeInterpreted(left.right().get(), right.right().get(), mergeType));
        }
    }

}
