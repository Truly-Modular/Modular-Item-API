package smartin.miapi.editor.material;

import imgui.ImGui;
import imgui.type.ImString;
import smartin.miapi.editor.util.ListEditor;
import smartin.miapi.material.CodecMaterial;

public class GroupEditor {
    private final CodecMaterial material;
    private final ListEditor<String> listEditor;

    public GroupEditor(CodecMaterial material) {
        this.material = material;
        this.listEditor = new ListEditor<>(
                "Groups",
                material.getGroups(),
                (nextGroups) -> {
                    material.groups = nextGroups;
                },
                (id, number) -> {
                    ImString string = new ImString(id);
                    return () -> {
                        ImGui.pushID(id + number);
                        ImGui.inputText("", string);
                        ImGui.popID();
                        return string.get();
                    };
                },
                () -> "new group"
        );
    }

    public void render() {
        listEditor.render();
    }
} 