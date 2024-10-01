package smartin.miapi.forge.mixin;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TranslatableContents.class)
public interface TranslatableTextContentAccessor {
    /*
    @Accessor
    List<StringV> getTranslations();

    @Accessor
    void setTranslations(List<StringVisitable> translations);
    
     */
}
