package smartin.miapi.editor;

import foundry.veil.api.client.editor.Inspector;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class VeilEditor {
    public static void setup() {
        VeilEventPlatform.INSTANCE.onVeilRendererAvailable((renderer) -> {
            if (VeilRenderSystem.hasImGui()) {
                Inspector inspector = new Inspector() {
                    @Override
                    public void render() {
                        MiapiEditor.renderAll(null, Minecraft.getInstance().getTimer());
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.literal("Miapi Widget");
                    }

                    @Override
                    public boolean isEnabled() {
                        return true;
                    }

                    @Override
                    public boolean isOpen() {
                        return true;
                    }
                };
                VeilRenderSystem.renderer().getEditorManager().add(inspector);
                VeilRenderSystem.renderer().getEditorManager().show(inspector);
            }

        });
    }
}
