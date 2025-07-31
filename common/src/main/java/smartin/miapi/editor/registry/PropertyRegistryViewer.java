package smartin.miapi.editor.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;

public class PropertyRegistryViewer extends RegistryViewer<ModuleProperty> {
    private final ImBoolean showTestEditor = new ImBoolean(false);
    private final ImString testJson = new ImString(1024);
    private final ImString testResult = new ImString(1024);
    private final ImBoolean testSuccess = new ImBoolean(false);

    public PropertyRegistryViewer() {
        super(RegistryInventory.MODULE_PROPERTY_MIAPI_REGISTRY);
        testJson.set("{\n  \"test\": \"value\"\n}");
    }

    @Override
    protected String getWindowTitle() {
        return "Property Registry";
    }

    @Override
    protected void renderEntry(Map.Entry<ResourceLocation, ModuleProperty> entry) {
        super.renderEntry(entry);
        if (ImGui.isItemClicked()) { // Right click
            showTestEditor.set(true);
            selectedValue = entry.getValue();
            selectedEntry.set(entry.getKey().toString());
        }
    }

    @Override
    protected void renderDetails(ModuleProperty property) {
        ImGui.text("Property: " + selectedEntry.get());
        ImGui.text("Type: " + property.getClass().getSimpleName());
        
        if (ImGui.button("Test Property")) {
            showTestEditor.set(true);
        }

        if (showTestEditor.get()) {
            renderTestEditor(property);
        }
    }

    private void renderTestEditor(ModuleProperty property) {
        ImGui.setNextWindowSize(400, 300, ImGuiCond.FirstUseEver);
        if (ImGui.begin("Test Property: " + selectedEntry.get()+"##"+ System.identityHashCode(this), showTestEditor)) {
            ImGui.text("Enter JSON to test property parsing:");
            
            if (ImGui.inputTextMultiline("##testJson", testJson, 
                    ImGui.getWindowWidth() - ImGui.getStyle().getWindowPaddingX() * 2,
                    100,
                    ImGuiInputTextFlags.AllowTabInput)) {
                testProperty(property);
            }

            ImGui.separator();
            
            if (testSuccess.get()) {
                ImGui.textColored(0.0f, 1.0f, 0.0f, 1.0f, "Test successful!");
            } else {
                ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, "Test failed!");
            }

            ImGui.text("Result:");
            ImGui.inputTextMultiline("##testResult", testResult,
                    ImGui.getWindowWidth() - ImGui.getStyle().getWindowPaddingX() * 2,
                    100,
                    ImGuiInputTextFlags.ReadOnly);

            if (ImGui.button("Close")) {
                showTestEditor.set(false);
            }
        }
        ImGui.end();
    }

    private void testProperty(ModuleProperty property) {
        try {
            JsonElement json = JsonParser.parseString(testJson.get());
            if(property.load(Miapi.id("runtime_test"),json,true)){
                Object result = property.decode(json);
                testResult.set(result != null ? result.toString() : "null");
                testSuccess.set(true);
            }
            testResult.set("property does not want to be loaded!°");
            testSuccess.set(false);
        } catch (Exception e) {
            testResult.set("Error: " + e.getMessage());
            testSuccess.set(false);
        }
    }
} 