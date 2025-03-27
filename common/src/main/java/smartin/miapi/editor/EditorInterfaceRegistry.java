package smartin.miapi.editor;

import dev.architectury.event.EventResult;
import smartin.miapi.editor.syntax.JsonSyntaxHighlighter;
import smartin.miapi.editor.syntax.PropertyMapHighlighter;

public class EditorInterfaceRegistry {
    public static void init() {
        // Register default JSON syntax highlighter
        EditorEvents.EDITOR_INTERFACES.register(event -> {
            if (event.filePath.endsWith(".json")) {
                event.interfaces.add(new JsonSyntaxHighlighter());
            }
            return EventResult.pass();
        });

        // Register PropertyMapHighlighter for module files
        EditorEvents.EDITOR_INTERFACES.register(event -> {
            if (event.resourceLocation != null &&
                event.resourceLocation.getPath().startsWith("miapi/modules/")) {
                event.interfaces.add(new PropertyMapHighlighter(event.resourceLocation));
            }
            return EventResult.pass();
        });
    }
} 