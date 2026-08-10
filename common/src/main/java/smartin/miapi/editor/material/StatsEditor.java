package smartin.miapi.editor.material;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import smartin.miapi.material.codec.CodecMaterial;

import java.util.ArrayList;
import java.util.List;

public class StatsEditor {
    private final CodecMaterial material;
    private final List<StatEntry> stats = new ArrayList<>();

    public StatsEditor(CodecMaterial material) {
        this.material = material;
        initializeStats();
    }

    private void initializeStats() {
        material.doubleMap.forEach((key, value) -> {
            try {
                double doubleValue = value;
                stats.add(new StatEntry(key, doubleValue));
            } catch (NumberFormatException ignored) {
                // Not a numeric stat
            }
        });
    }

    public void render() {
        if (ImGui.collapsingHeader("Stats", ImGuiTreeNodeFlags.DefaultOpen)) {
            new ArrayList<>(stats).forEach(entry -> {
                ImGui.pushID(entry.toString());
                ImGui.pushItemWidth(150); // Limit width to 150 pixels
                if (ImGui.inputText("Stat", entry.name, ImGuiInputTextFlags.None)) {
                    try {
                        material.doubleMap.put(entry.name.get(), entry.value.get());
                    } catch (Exception e) {
                        ImGui.text("Invalid stat value!");
                    }
                }
                ImGui.popItemWidth(); // Restore default width
                ImGui.sameLine();

                ImGui.pushItemWidth(50); // Limit width to 50 pixels
                if (ImGui.inputDouble("Value", entry.value)) {
                    material.doubleMap.put(entry.name.get(), entry.value.get());
                }
                ImGui.popItemWidth();
                ImGui.sameLine();

                if (ImGui.button("Remove")) {
                    stats.remove(entry);
                    material.doubleMap.remove(entry.name.get());
                }

                ImGui.popID();
            });

            if (ImGui.button("Add Stat")) {
                String newStatName = "new_stat";
                material.doubleMap.put(newStatName, 0.0);
                stats.add(new StatEntry(newStatName, 0.0));
            }
        }
    }

    private static class StatEntry {
        final ImString name;
        final imgui.type.ImDouble value;

        StatEntry(String name, double value) {
            this.name = new ImString(128);
            this.name.set(name);
            this.value = new imgui.type.ImDouble(value);
        }
    }
} 