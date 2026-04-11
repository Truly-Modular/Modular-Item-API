package smartin.miapi.editor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import smartin.miapi.Miapi;
import smartin.miapi.editor.registry.RegistryViewer;
import smartin.miapi.editor.syntax.EditorInterface;
import smartin.miapi.modules.properties.util.EditorError;
import smartin.miapi.registries.MiapiRegistry;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonEditor implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final ImString content = new ImString(64000);
    private final Consumer<String> onChange;
    private JsonElement currentJson;
    private final Map<ResourceLocation, EditorInterface> interfaces = new HashMap<>();
    private final List<EditorInterface> activeInterfaces = new ArrayList<>();
    private final List<EditorError> currentErrors = new ArrayList<>();
    private static final Map<ResourceLocation, EditorInterface> GLOBAL_INTERFACES = new HashMap<>();
    public static int padding = 2;
    public boolean showErrors = false;
    private boolean readOnly = false;
    private boolean watchFile = true;
    private boolean reloadOnChange = false;
    private Path filePath;
    private WatchService watchService;
    private WatchKey watchKey;
    private long lastModified = 0;
    public ResourceLocation resourceLocation;
    public boolean closeOnNoError = false;

    public JsonEditor(String initialContent, Consumer<String> onChange) {
        this(initialContent, onChange, null, null);
    }

    public JsonEditor(String initialContent, Consumer<String> onChange, Path filePath) {
        this(initialContent, onChange, filePath, null);
    }

    public JsonEditor(String initialContent, Consumer<String> onChange, Path filePath, ResourceLocation resourceLocation) {
        this.content.set(initialContent);
        this.onChange = onChange;
        this.filePath = filePath;
        this.resourceLocation = resourceLocation;

        // Get interfaces through event system
        if (resourceLocation != null && filePath != null) {
            List<EditorInterface> eventInterfaces = new ArrayList<>();
            EditorEvents.EDITOR_INTERFACES.invoker().onGetInterfaces(
                    new EditorEvents.EditorInterfaceData(resourceLocation, filePath.toString(), eventInterfaces)
            );

            eventInterfaces.forEach(iface -> {
                interfaces.put(iface.getId(), iface);
                activeInterfaces.add(iface);
            });
        }

        validateContent();
        setupFileWatcher();
    }

    private void setupFileWatcher() {
        if (filePath != null) {
            try {
                watchService = filePath.getFileSystem().newWatchService();
                watchKey = filePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                lastModified = Files.getLastModifiedTime(filePath).toMillis();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean skipNext = false;

    private void checkFileChanges() {
        if (!watchFile || watchService == null) return;

        try {
            WatchKey key = watchService.poll();
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (filePath.getFileName().equals(changed)) {
                        long newLastModified = Files.getLastModifiedTime(filePath).toMillis();
                        if (newLastModified > lastModified) {
                            if (skipNext) {
                                skipNext = !skipNext;
                                return;
                            }
                            lastModified = newLastModified;
                            reloadFile();
                            if (reloadOnChange) {
                                onChange.accept(content.get());
                            }
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadFile() {
        if (filePath != null) {
            try {
                String newContent = Files.readString(filePath);
                content.set(newContent);
                validateContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public static void registerGlobalInterface(EditorInterface editorInterface) {
        GLOBAL_INTERFACES.put(editorInterface.getId(), editorInterface);
    }

    public void addInterface(EditorInterface editorInterface) {
        interfaces.put(editorInterface.getId(), editorInterface);
        activeInterfaces.add(editorInterface);
        validateContent();
    }

    public void removeInterface(ResourceLocation id) {
        EditorInterface removed = interfaces.remove(id);
        if (removed != null) {
            activeInterfaces.remove(removed);
            validateContent();
        }
    }

    private void validateContent() {
        currentErrors.clear();
        try {
            currentJson = JsonParser.parseString(content.get());
            for (EditorInterface iface : activeInterfaces) {
                currentErrors.addAll(iface.validateContent(currentJson, content.get(),0 ));
            }
        } catch (Exception e) {
            currentJson = null;
            currentErrors.add(new EditorError(
                    getLineNumber(content.get(), e.getMessage()),
                    e.getMessage(),
                    EditorError.ErrorSeverity.ERROR
            ));
        }
    }

    private int getLineNumber(String content, String errorMessage) {
        // Try to extract line number from common JSON parser error messages
        // This is a basic implementation and might need to be enhanced based on your JSON parser
        try {
            if (errorMessage.contains("line")) {
                String[] parts = errorMessage.split("line");
                return Integer.parseInt(parts[1].trim().split(" ")[0]);
            }
        } catch (Exception e) {
            // Fallback if we can't parse the line number
        }
        return 1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!show.get()) {
            MiapiEditor.editors.remove(this);
            return;
        }
        boolean hasErrors = false;

        // Check for file changes
        checkFileChanges();

        ImGui.pushID(resourceLocation.toString());
        ImGui.setNextWindowSize(800, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin("JSON Editor " + resourceLocation+"##"+ System.identityHashCode(this), show)) {
            float windowWidth = ImGui.getWindowWidth();
            float windowHeight = ImGui.getWindowHeight();

            // Toolbar at the top
            if (ImGui.button("Save") && currentJson != null && !readOnly) {
                save(content.get());
                onChange.accept(content.get());
            }
            ImGui.sameLine();
            if (ImGui.button("Format") && !readOnly) {
                try {
                    if (currentJson != null) {
                        content.set(Miapi.gson.toJson(currentJson));
                        validateContent();
                    }
                } catch (Exception ignored) {
                }
            }
            if (filePath != null) {
                ImGui.sameLine();
                if (ImGui.button("Reload")) {
                    reloadFile();
                }
                ImGui.sameLine();
                if (ImGui.checkbox("Watch File", watchFile)) {
                    watchFile = !watchFile;
                }
                ImGui.sameLine();
                if (ImGui.checkbox("Auto Reload", reloadOnChange)) {
                    reloadOnChange = !reloadOnChange;
                }
            }
            // Dropdown menu
            if (ImGui.button("Registries")) {
                ImGui.openPopup("DropdownMenu"+resourceLocation);
            }
            if (ImGui.beginPopup("DropdownMenu"+resourceLocation)) {
                for (MiapiRegistry<?> registry : MiapiRegistry.REGISTRY_MAP.values()) {
                    if (ImGui.menuItem(registry.getName())) {
                        MiapiEditor.editors.add(new RegistryViewer<>(registry));
                    }
                }
                ImGui.endPopup();
            }
            if (readOnly) {
                ImGui.sameLine();
                ImGui.textColored(1.0f, 0.7f, 0.0f, 1.0f, "Read Only");
            }

            // Add toolbar buttons from interfaces
            for (EditorInterface iface : activeInterfaces) {
                Map<String, Runnable> buttons = iface.toolbarButtons();
                for (Map.Entry<String, Runnable> button : buttons.entrySet()) {
                    ImGui.sameLine();
                    if (ImGui.button(button.getKey())) {
                        button.getValue().run();
                    }
                }
            }

            float toolbarHeight = ImGui.getFrameHeightWithSpacing();  // Height for toolbar
            String[] lines = content.get().split("\n", -1);
            float lineHeight = ImGui.getTextLineHeight();
            float desiredHeight = lines.length * lineHeight + ImGui.getStyle().getWindowPaddingY() * 2;
            float contentHeight = windowHeight - toolbarHeight - ImGui.getStyle().getWindowPaddingY() * 8;
            ImGui.separator();

            // Calculate gutter width (for line numbers and error indicators)
            float gutterWidth = 40;  // Base width for line numbers
            float editorWidth = windowWidth - ImGui.getStyle().getWindowPaddingX() * 2;
            float mainEditorWidth = editorWidth - gutterWidth;

            // Create map of line numbers to errors for quick lookup
            Map<Integer, EditorError> errorsByLine = new HashMap<>();
            for (EditorError error : currentErrors) {
                errorsByLine.put(error.line(), error);
            }

            // Main editor container
            if (ImGui.beginChild("EditorContainer"+resourceLocation, editorWidth, contentHeight, true)) {
                if (ImGui.beginChild("EditorScrollContainer"+resourceLocation, editorWidth, desiredHeight + ImGui.getStyle().getWindowPaddingY() * 2, true)) {
                    // Store scroll position to sync gutter and editor
                    float scrollY = ImGui.getScrollY();

                    // Left gutter for line numbers and error indicators
                    ImGui.beginChild("Gutter"+resourceLocation, gutterWidth, desiredHeight, false);
                    ImGui.setScrollY(ImGui.getScrollY());
                    float currentY = 0;

                    for (int i = 0; i < lines.length; i++) {
                        ImGui.setCursorPosY(currentY);

                        // Check if line has error
                        EditorError error = errorsByLine.get(i + 1);
                        if (error != null) {
                            hasErrors = true;
                            float[] color = getErrorColor(error.severity());
                            ImGui.pushStyleColor(ImGuiCol.Text, color[0], color[1], color[2], 1.0f);
                            ImGui.text("⚠" + (i + 1));
                            if (ImGui.isItemHovered()) {
                                ImGui.beginTooltip();
                                ImGui.pushTextWrapPos(300);
                                ImGui.textColored(color[0], color[1], color[2], 1.0f, error.message());
                                ImGui.popTextWrapPos();
                                ImGui.endTooltip();
                            }
                            ImGui.popStyleColor();
                        } else {
                            // Line number
                            ImGui.textDisabled("" + (i + 1));
                        }
                        currentY += lineHeight;
                    }
                    ImGui.endChild();

                    // Main editor
                    ImGui.sameLine();
                    ImGui.beginChild("MainEditor"+resourceLocation, mainEditorWidth, desiredHeight, false);

                    // Ensure both scroll positions stay in sync
                    ImGui.setScrollY(scrollY);

                    // Update input flags for read-only mode
                    int inputFlags = ImGuiInputTextFlags.AllowTabInput;
                    if (readOnly) {
                        inputFlags |= ImGuiInputTextFlags.ReadOnly;
                    }

                    if (ImGui.isWindowFocused()) {
                        if (ImGui.inputTextMultiline("##content" + resourceLocation, content,
                                mainEditorWidth - ImGui.getStyle().getWindowPaddingX(),
                                desiredHeight,
                                inputFlags)) {
                            validateContent();  // Only update content when window is focused
                        }
                    } else {
                        ImGui.textUnformatted(content.get());  // Display content but prevent edits
                    }
                    ImGui.endChild();

                    ImGui.endChild();
                }
                ImGui.endChild();
            }
            ImGui.end();
        }
        ImGui.popID();
        if (closeOnNoError && !hasErrors) {
            save(content.get());
            this.close();
            MiapiEditor.editors.remove(this);
        }
    }

    public void save(String newContent) {
        try {
            skipNext = true;
            Files.writeString(filePath, newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private float[] getErrorColor(EditorError.ErrorSeverity severity) {
        return switch (severity) {
            case ERROR -> new float[]{1.0f, 0.0f, 0.0f, 1.0f};
            case WARNING -> new float[]{1.0f, 0.8f, 0.0f, 1.0f};
            case INFO -> new float[]{0.0f, 0.8f, 1.0f, 1.0f};
        };
    }

    private sealed interface Section permits ContentSection, ErrorSection {
    }

    private record ContentSection(int start, int end, String[] lines) implements Section {
    }

    private record ErrorSection(int line, EditorError error) implements Section {
    }

    @Override
    public void close() {
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}