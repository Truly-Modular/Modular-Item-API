package smartin.miapi.editor.syntax;

import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;

public class PropertyHolderEditor extends CompositeEditor {

    public static final ResourceLocation ID = Miapi.id("property_holder_editor");

    public PropertyHolderEditor() {

        // optional sections (codec uses optionalFieldOf)
        addEditor("replace", false, new PropertyMapHighlighter());
        addEditor("merge", false, new PropertyMapHighlighter());
        addEditor("remove", false, new RemoveListEditor());
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }
}