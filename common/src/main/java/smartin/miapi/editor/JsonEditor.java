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

import java.util.*;
import java.util.function.Consumer;

public class JsonEditor implements MiapiEditor {
    private final ImBoolean show = new ImBoolean(true);
    private final ImString content = new ImString(4096);
    private final Consumer<String> onChange;
    private JsonElement currentJson;
    private final Map<ResourceLocation, EditorInterface> interfaces = new HashMap<>();
    private final List<EditorInterface> activeInterfaces = new ArrayList<>();
    private final List<EditorInterface.EditorError> currentErrors = new ArrayList<>();
    private static final Map<ResourceLocation, EditorInterface> GLOBAL_INTERFACES = new HashMap<>();
    public static int padding = 2;
    public boolean showErrors = false;

    public JsonEditor(String initialContent, Consumer<String> onChange) {
        this(initialContent, onChange, Collections.emptyList());
    }

    public JsonEditor(String initialContent, Consumer<String> onChange, List<ResourceLocation> defaultInterfaces) {
        this.content.set(initialContent);
        this.onChange = onChange;
        defaultInterfaces.forEach(id -> {
            EditorInterface iface = GLOBAL_INTERFACES.get(id);
            if (iface != null) {
                interfaces.put(id, iface);
                activeInterfaces.add(iface);
            }
        });
        validateContent();
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
                currentErrors.addAll(iface.validateContent(currentJson, content.get()));
            }
        } catch (Exception e) {
            currentJson = null;
            currentErrors.add(new EditorInterface.EditorError(
                    getLineNumber(content.get(), e.getMessage()),
                    e.getMessage(),
                    EditorInterface.EditorError.ErrorSeverity.ERROR
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
        if (!show.get()) return;

        ImGui.setNextWindowSize(800, 600, ImGuiCond.FirstUseEver);
        if (ImGui.begin("JSON Editor", show)) {
            float windowWidth = ImGui.getWindowWidth();
            float windowHeight = ImGui.getWindowHeight();
            float buttonHeight = 50;
            float contentHeight = windowHeight - buttonHeight - ImGui.getStyle().getWindowPaddingY() * 2;
            float lineHeight = ImGui.getTextLineHeight();

            // Calculate gutter width (for line numbers and error indicators)
            float gutterWidth = 40;  // Base width for line numbers
            float editorWidth = windowWidth - ImGui.getStyle().getWindowPaddingX() * 2;
            float mainEditorWidth = editorWidth - gutterWidth;

            // Create map of line numbers to errors for quick lookup
            Map<Integer, EditorInterface.EditorError> errorsByLine = new HashMap<>();
            for (EditorInterface.EditorError error : currentErrors) {
                errorsByLine.put(error.line(), error);
            }

            // Main editor container
            if (ImGui.beginChild("EditorContainer", editorWidth, contentHeight, true)) {
                // Store scroll position to sync gutter and editor
                float scrollY = ImGui.getScrollY();
                int visibleLines = (int) (contentHeight / lineHeight);

                // Left gutter for line numbers and error indicators
                ImGui.beginChild("Gutter", gutterWidth, contentHeight - ImGui.getStyle().getScrollbarSize(), false);
                String[] lines = content.get().split("\n", -1);
                float currentY = 0;

                for (int i = 0; i < lines.length; i++) {
                    // Only render visible line numbers
                    if (currentY >= scrollY - lineHeight && currentY <= scrollY + contentHeight) {
                        ImGui.setCursorPosY(currentY);

                        // Check if line has error
                        EditorInterface.EditorError error = errorsByLine.get(i + 1);
                        if (error != null) {
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
                    }
                    currentY += lineHeight;
                }
                ImGui.endChild();

                // Main editor
                ImGui.sameLine();
                ImGui.beginChild("MainEditor", mainEditorWidth, contentHeight - ImGui.getStyle().getScrollbarSize(), false);

                // Ensure both scroll positions stay in sync
                ImGui.setScrollY(scrollY);

                if (ImGui.inputTextMultiline("##content", content,
                        mainEditorWidth - ImGui.getStyle().getWindowPaddingX(),
                        contentHeight - ImGui.getStyle().getWindowPaddingY() * 2,
                        ImGuiInputTextFlags.AllowTabInput)) {
                    validateContent();
                }
                ImGui.endChild();

                ImGui.endChild();
            }

            // Button row at bottom
            ImGui.separator();
            if (ImGui.button("Save") && currentJson != null) {
                onChange.accept(content.get());
            }
            ImGui.sameLine();
            if (ImGui.button("Format")) {
                try {
                    if (currentJson != null) {
                        content.set(currentJson.toString());
                        validateContent();
                    }
                } catch (Exception ignored) {
                }
            }

            ImGui.end();
        }
    }

    private float[] getErrorColor(EditorInterface.EditorError.ErrorSeverity severity) {
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

    private record ErrorSection(int line, EditorInterface.EditorError error) implements Section {
    }
}