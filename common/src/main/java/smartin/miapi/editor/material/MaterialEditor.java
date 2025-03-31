package smartin.miapi.editor.material;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.MiapiEditor;
import smartin.miapi.editor.util.ListEditor;
import smartin.miapi.material.CodecMaterial;

import java.util.HashMap;
import java.util.function.Consumer;

public class MaterialEditor implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final ImString materialId = new ImString(128);
    private final CodecMaterial material;
    private final Consumer<CodecMaterial> onChange;

    private final PropertyEditor regularPropertyEditor;
    private final PropertyEditor displayPropertyEditor;
    private final PropertyEditor hiddenPropertyEditor;
    private final IconEditor iconEditor;
    private final IngredientEditor ingredientEditor;
    private final StatsEditor statsEditor;
    private final GroupEditor groupEditor;
    private final ListEditor<String> textureKeys;

    public MaterialEditor(CodecMaterial material, Consumer<CodecMaterial> onChange) {
        this.material = material;
        this.onChange = onChange;
        this.materialId.set(material.getID().toString());

        // Initialize specialized editors
        this.regularPropertyEditor = new PropertyEditor("Properties", material.getActualProperty()
                .computeIfAbsent("default", (a) ->
                        material.getActualProperty()
                                .computeIfAbsent(a, (s) -> new HashMap<>())));
        this.displayPropertyEditor = new PropertyEditor("Display Properties", material.getActualProperty()
                .computeIfAbsent("default", (a) ->
                        material.getActualProperty()
                                .computeIfAbsent(a, (s) -> new HashMap<>())));
        this.hiddenPropertyEditor = new PropertyEditor("Hidden Properties", material.getActualProperty()
                .computeIfAbsent("default", (a) ->
                        material.getActualProperty()
                                .computeIfAbsent(a, (s) -> new HashMap<>())));
        this.iconEditor = new IconEditor(material);
        this.ingredientEditor = new IngredientEditor(material);
        this.statsEditor = new StatsEditor(material);
        this.groupEditor = new GroupEditor(material);
        this.textureKeys = new ListEditor<>(
                "Texture Keys",
                material.getTextureKeys(),
                (nextGroups) -> {
                    material.textureKeys = nextGroups;
                },
                (id, number) -> {
                    ImString string = new ImString(id);
                    return () -> {
                        ImGui.pushID("texture" + id + number);
                        ImGui.inputText("", string);
                        ImGui.popID();
                        return string.get();
                    };
                },
                () -> "texture"
        );
    }

    private void renderMaterialProperties() {
        ImGui.separator();

        // Material ID
        if (ImGui.inputText("Material ID", materialId, ImGuiInputTextFlags.None)) {
            try {
                ResourceLocation id = Miapi.id(materialId.get());
                material.setID(id);
            } catch (Exception e) {
                ImGui.text("Invalid ID format!");
            }
        }

        ImGui.separator();

        // Render specialized editors
        iconEditor.render();
        statsEditor.render();
        ingredientEditor.render();
        this.groupEditor.render();
        this.textureKeys.render();

        if (ImGui.collapsingHeader("Properties")) {
            regularPropertyEditor.render();
            displayPropertyEditor.render();
            hiddenPropertyEditor.render();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) return;

        ImGui.setNextWindowSize(600, 400, ImGuiCond.FirstUseEver);
        if (ImGui.begin("Material Editor", show)) {
            renderMaterialProperties();

            if (ImGui.button("Save")) {
                onChange.accept(material);
            }
        }
        ImGui.end();
    }
} 