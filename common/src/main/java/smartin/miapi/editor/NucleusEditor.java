package smartin.miapi.editor;

import com.redpxnda.nucleus.editor.core.ClientLoader;

public class NucleusEditor {
    public static void setup() {
        ClientLoader.RENDER.add(MiapiEditor::renderAll);
    }
}
