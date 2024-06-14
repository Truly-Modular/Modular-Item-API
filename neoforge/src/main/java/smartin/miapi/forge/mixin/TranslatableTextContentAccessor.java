package smartin.miapi.forge.mixin;

import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TranslatableTextContent.class)
public interface TranslatableTextContentAccessor {
    @Accessor
    List<StringVisitable> getTranslations();

    @Accessor
    void setTranslations(List<StringVisitable> translations);
}
