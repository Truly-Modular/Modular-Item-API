package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import smartin.miapi.modules.CodecModuleInheritance;
import smartin.miapi.modules.properties.util.EditorError;

import java.util.List;

public class ModuleValidatorInterface extends CodecValidatorInterface {

    public ModuleValidatorInterface() {
        super(CodecModuleInheritance.CODEC, "module_inheritens");
    }

    public List<EditorError> validateContent(JsonElement json, String rawContent, int lineOffset) {
        if (json instanceof JsonObject object) {
            if (!object.has("parent")) {
                return PropertyMapHighlighter.getEditorErrors(rawContent, object,lineOffset);
            }
        }
        return super.validateContent(json, rawContent, lineOffset);
    }
}
