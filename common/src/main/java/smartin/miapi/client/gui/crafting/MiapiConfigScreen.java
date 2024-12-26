package smartin.miapi.client.gui.crafting;

import com.redpxnda.nucleus.config.ConfigManager;
import com.redpxnda.nucleus.config.ConfigObject;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import smartin.miapi.Miapi;

public class MiapiConfigScreen extends Screen {
    private final Screen parent;

    public MiapiConfigScreen(Screen parent) {
        super(Component.translatable("miapi.config_screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Button to open the Client Config screen
        addRenderableWidget(Button.builder(Component.translatable("miapi.config_screen.client"), button -> {
            var config = ConfigManager.getConfigObject(Miapi.id("client"));
            if (config instanceof ConfigObject.Automatic<Object> automatic && minecraft != null) {
                minecraft.setScreen(automatic.getScreen(this));
            }
        }).bounds(this.width / 2 - 100, this.height / 2 - 30, 200, 20).build());

        // Button to open the Server Config screen
        addRenderableWidget(Button.builder(Component.translatable("miapi.config_screen.server"), button -> {
            var config = ConfigManager.getConfigObject(Miapi.id("server"));
            if (config instanceof ConfigObject.Automatic<Object> automatic && minecraft != null) {
                minecraft.setScreen(automatic.getScreen(this));
            }
        }).bounds(this.width / 2 - 100, this.height / 2 + 10, 200, 20).build());

        // Back button to return to the parent screen
        addRenderableWidget(Button.builder(Component.translatable("miapi.config_screen.back"), button -> {
            minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 50, this.height - 40, 100, 20).build());
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);
    }
}
