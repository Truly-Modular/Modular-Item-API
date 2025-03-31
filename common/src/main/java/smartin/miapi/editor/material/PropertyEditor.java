package smartin.miapi.editor.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertyEditor {
    private final List<PropertyEntry> propertyData = new ArrayList<>();
    private final String title;
    private final Map<ModuleProperty<?>, Object> propertyMap;
    private boolean showPropertyDropdown = false;
    private final ImString searchText = new ImString(128);

    public PropertyEditor(String title, Map<ModuleProperty<?>, Object> propertyMap) {
        this.title = title;
        this.propertyMap = propertyMap;
        initializeProperties();
    }

    private void initializeProperties() {
        propertyMap.forEach((prop, value) -> {
            propertyData.add(new PropertyEntry(prop, value));
        });
    }

    public void render() {
        if (ImGui.collapsingHeader(title, ImGuiTreeNodeFlags.DefaultOpen)) {
            new ArrayList<>(propertyData).forEach(entry -> {
                ImGui.pushID(entry.toString());

                ImGui.text(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.findKey(entry.property).toString());

                if (ImGui.inputText("Value", entry.value, ImGuiInputTextFlags.None)) {
                    try {
                        JsonElement element = JsonParser.parseString(entry.value.get());
                        propertyMap.put(entry.property, element);
                    } catch (RuntimeException e) {
                        ImGui.text("Invalid data!");
                    }
                }

                if (ImGui.button("Remove")) {
                    propertyData.remove(entry);
                    propertyMap.remove(entry.property);
                }

                ImGui.popID();
            });

            if (ImGui.button("Add Property")) {
                showPropertyDropdown = true;
                searchText.set("");
            }

            if (showPropertyDropdown) {
                ImGui.openPopup("Property Selection");
                if (ImGui.beginPopupModal("Property Selection")) {
                    if (ImGui.inputText("Search", searchText, ImGuiInputTextFlags.None)) {
                        // Filter properties based on search text
                    }

                    ImGui.beginChild("Property List", 200, 200);
                    RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY.getFlatMap().forEach((id, property) -> {
                        if (searchText.get().isEmpty() ||
                            id.toString().toLowerCase().contains(searchText.get().toLowerCase())) {
                            if (ImGui.selectable(id.toString())) {
                                propertyData.add(new PropertyEntry(property, "{}"));
                                propertyMap.put(property, JsonParser.parseString("{}"));
                                showPropertyDropdown = false;
                            }
                        }
                    });
                    ImGui.endChild();

                    if (ImGui.button("Cancel")) {
                        showPropertyDropdown = false;
                    }
                    ImGui.endPopup();
                }
            }
        }
    }

    private static class PropertyEntry {
        final ModuleProperty<?> property;
        final ImString value;

        PropertyEntry(ModuleProperty<?> property, Object value) {
            this.property = property;
            this.value = new ImString(1024);
            this.value.set(value.toString());
        }
    }
} 