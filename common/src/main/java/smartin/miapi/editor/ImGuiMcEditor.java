package smartin.miapi.editor;

import foundry.imgui.api.ImGuiMCEvents;
import foundry.imgui.api.event.RenderImGuiEvents;
import net.minecraft.client.Minecraft;

public class ImGuiMcEditor {
    public static void setup() {
        ImGuiMCEvents.INSTANCE.preRenderImGuiEvent(new RenderImGuiEvents.Pre() {
            @Override
            public void drawImGuiPre() {
                MiapiEditor.renderAll(null, Minecraft.getInstance().getTimer());
            }
        });
    }
}
