package smartin.miapi.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.ModuleInstance;
import smartin.miapi.registries.RegistryInventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ModuleEditor implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final ImString itemModuleName = new ImString(128);
    private final ModuleInstance module;
    Consumer<ModuleInstance> onChange;
    public Map<String, ModuleEditor> subModuleEditors = new LinkedHashMap<>();
    public List<Pair<ImString, ImString>> moduleData = new ArrayList();

    public ModuleEditor(ModuleInstance module, Consumer<ModuleInstance> onChange) {
        this.module = module;
        this.onChange = onChange;
        itemModuleName.set(module.moduleID);
        module.subModules.forEach((id, m) -> {
            subModuleEditors.put(id, new ModuleEditor(m, (change -> {
                module.setSubModule(id, change);
                onChange.accept(module);
            })));
        });
        module.moduleData.forEach((id, data) -> {
            ImString imString = new ImString(128);
            imString.set(id.toString());
            moduleData.add(new Pair<>(of(id.toString()), of(data.toString())));
        });
    }

    private void renderModuleInstance() {
        ImGui.separator();
        if (ImGui.inputText("Item Module", itemModuleName, ImGuiInputTextFlags.None)) {
            ResourceLocation id = Miapi.id(itemModuleName.get());
            ItemModule module1 = RegistryInventory.ITEM_MODULE_MIAPI_REGISTRY.get(Miapi.id(itemModuleName.get()));
            if (module1 != null) {
                module.setModule(module1);
                module.moduleID = id;
            } else {
                ImGui.text("invalid ID");
            }
        }

        ImGui.separator();

        if (ImGui.collapsingHeader("Submodules", ImGuiTreeNodeFlags.DefaultOpen)) {
            subModuleEditors.forEach((id, editor) -> {
                if (ImGui.treeNode(id)) {
                    editor.renderModuleInstance();
                    ImGui.treePop();
                }
            });
        }

        ImGui.separator();

        if (ImGui.collapsingHeader("Module Data", ImGuiTreeNodeFlags.DefaultOpen)) {
            moduleData.forEach((pair) -> {
                ImGui.pushID(pair.getFirst().toString());
                ImString id = pair.getFirst();
                ImString data = pair.getSecond();
                String old = id.get();
                if (ImGui.inputText("Key", id, ImGuiInputTextFlags.None)) {
                    try {
                        JsonElement element = JsonParser.parseString(data.get());
                        module.moduleData.remove(Miapi.id(old));
                        module.moduleData.put(Miapi.id(id.get()), element);
                    } catch (RuntimeException e) {
                        ImGui.text("invalid data!");
                    }
                }
                //ImGui.sameLine();
                if (ImGui.inputText("value", data, ImGuiInputTextFlags.None)) {
                    try {
                        JsonElement element = JsonParser.parseString(data.get());
                        module.moduleData.put(Miapi.id(id.get()), element);
                    } catch (RuntimeException e) {
                        ImGui.text("invalid data!");
                    }
                }
                //ImGui.sameLine();
                if (ImGui.button("Remove")) {
                    moduleData.remove(pair);
                    module.moduleData.remove(Miapi.id(id.get()));
                }
                ImGui.popID();
            });

            if (ImGui.button("Add Entry")) {
                module.moduleData.put(ResourceLocation.parse("miapi:new_data"), JsonParser.parseString("{}"));
                moduleData.add(new Pair<>(of("miapi:new_data"), of("{}")));
            }
        }
    }

    public static ImString of(String string) {
        ImString imString = new ImString(1024);
        imString.set(string);
        return imString;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }

        ImGui.setNextWindowSize(400, 300, ImGuiCond.FirstUseEver);
        if (ImGui.begin("Module Editor##"+ System.identityHashCode(this), show)) {
            renderModuleInstance();
        }
        if (ImGui.button("Save")) {
            onChange.accept(module);
        }
        ImGui.end();
    }
}
