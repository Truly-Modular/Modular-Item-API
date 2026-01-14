package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.modules.properties.util.Validator;
import smartin.miapi.registries.JsonOpsBooleanPatched;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecValidatorInterface implements EditorInterface {
    private final Codec<?> codec;
    private final ResourceLocation id;
    private final String name;

    public CodecValidatorInterface(Codec<?> codec, String name) {
        this.codec = codec;
        this.name = name;
        this.id = Miapi.id("codec_validator", name.toLowerCase().replace(" ", "_"));
    }

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent) {
        List<EditorError> errors = new ArrayList<>();

        if (json == null) {
            errors.add(new EditorError(1, "Invalid JSON", EditorError.ErrorSeverity.ERROR));
            return errors;
        }

        try {
            DataResult<?> result = codec.decode(NbtOps.INSTANCE, JsonOpsBooleanPatched.INSTANCE.convertTo(NbtOps.INSTANCE, Miapi.gson.fromJson(rawContent, JsonElement.class)));
            result.result().ifPresentOrElse(
                    value -> {
                        if(value instanceof Pair pair){
                            errors.addAll(getErrorsFromValidator(pair.getFirst()));
                        }
                    },
                    () -> errors.add(new EditorError(1, "Failed to decode " + name + ": " + result.error().get().message(), EditorError.ErrorSeverity.ERROR))
            );
        } catch (Exception e) {
            errors.add(new EditorError(1, "Failed to validate " + name + ": " + e.getMessage(), EditorError.ErrorSeverity.ERROR));
        }

        return errors;
    }

    public <T> List<EditorError> getErrorsFromValidator(T data) {
        try {
            return ((Validator<T>) data).validate(1, data, true);
        } catch (ClassCastException ignored) {

        }
        return List.of();
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        // We'll use the same highlighting as JsonSyntaxHighlighter
        return new HashMap<>();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }
} 