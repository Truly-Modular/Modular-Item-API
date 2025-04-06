package smartin.miapi.editor.registry;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.editor.DocPage;
import smartin.miapi.editor.MiapiEditor;
import smartin.miapi.registries.MiapiRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RegistryViewer<T> implements MiapiEditor {
    protected final ImBoolean show = new ImBoolean(true);
    protected final MiapiRegistry<T> registry;
    protected final ImString searchText = new ImString(256);
    protected final ImString selectedEntry = new ImString(256);
    protected final List<Map.Entry<ResourceLocation, T>> filteredEntries = new ArrayList<>();
    protected final ImBoolean showDetails = new ImBoolean(false);
    protected T selectedValue;
    protected Consumer<T> onSelect;

    public RegistryViewer(MiapiRegistry<T> registry) {
        this.registry = registry;
        updateFilteredEntries();
    }

    protected void updateFilteredEntries() {
        filteredEntries.clear();
        String search = searchText.get().toLowerCase();
        registry.getFlatMap().entrySet().stream()
                .filter(entry -> entry.getKey().toString().toLowerCase().contains(search))
                .forEach(filteredEntries::add);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) return;

        ImGui.setNextWindowSize(800, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin(getWindowTitle(), show)) {
            // Search bar
            if (ImGui.inputText("Search", searchText)) {
                updateFilteredEntries();
            }

            ImGui.separator();

            // Registry content
            if (ImGui.beginChild("RegistryContent", 0, -ImGui.getFrameHeightWithSpacing(), true)) {
                for (Map.Entry<ResourceLocation, T> entry : filteredEntries) {
                    if (ImGui.treeNodeEx(entry.getKey().toString(), ImGuiTreeNodeFlags.None)) {
                        renderEntry(entry);
                        ImGui.treePop();
                    }
                }
            }
            ImGui.endChild();

            // Details panel
            if (showDetails.get() && selectedValue != null) {
                ImGui.sameLine();
                if (ImGui.beginChild("DetailsPanel", 300, 0, true)) {
                    renderDetails(selectedValue);
                }
                ImGui.endChild();
            }
        }
        ImGui.end();
    }

    protected String getWindowTitle() {
        return "Registry " + registry.getName();
    }

    protected void renderEntry(Map.Entry<ResourceLocation, T> entry) {
        selectedValue = entry.getValue();
        selectedEntry.set(entry.getKey().toString());
        if (onSelect != null) {
            onSelect.accept(selectedValue);
        }
        if (DocPage.PAGE_LOOKUP.containsKey(entry.getValue().getClass())) {
            DocPage page = DocPage.PAGE_LOOKUP.get(entry.getValue().getClass());

                // Header (maybe bold)
            ImGui.textColored(1f, 1f, 0f, 1f, page.header); // yellowish

            ImGui.separator();

            // Description with support for newlines
            if (page.description != null && !page.description.isEmpty()) {
                for (String line : page.description.split("\n")) {
                    ImGui.textWrapped(line);
                }
            }

            // Spacer
            ImGui.spacing();
            ImGui.separator();
            ImGui.text("Details:");

            // Key-Value data rendering
            for (Map.Entry<String, String> dataEntry : page.data.entrySet()) {
                ImGui.bulletText("%s: %s".formatted(dataEntry.getKey(), dataEntry.getValue()));
            }

        }
        showDetails.set(true);
    }

    protected void renderDetails(T value) {

    }

    public void setOnSelect(Consumer<T> onSelect) {
        this.onSelect = onSelect;
    }

    public T getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(T value) {
        this.selectedValue = value;
        if (value != null) {
            ResourceLocation key = registry.findKey(value);
            if (key != null) {
                selectedEntry.set(key.toString());
                showDetails.set(true);
            }
        }
    }

    public interface DetailSupplier<T> {
        T details(ResourceLocation id, T entry);
    }
} 