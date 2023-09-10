package smartin.miapi.client.gui.crafting;

import com.redpxnda.nucleus.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.crafting.crafter.ModuleCrafter;
import smartin.miapi.client.gui.crafting.slotdisplay.SlotDisplay;
import smartin.miapi.client.gui.crafting.slotdisplay.SmithDisplay;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ViewMinimizeButton extends InteractAbleWidget {
    private Supplier<ModuleCrafter> moduleCrafter;
    private Consumer<ModuleCrafter> moduleCrafterConsumer;
    private Supplier<SlotDisplay> slotDisplay;
    private Supplier<SmithDisplay> smithDisplay;
    private final Consumer<InteractAbleWidget> remover;
    private final Consumer<InteractAbleWidget> adder;
    private boolean isEnabled = true;
    public int minimizeAmount = 75;

    public ViewMinimizeButton(int x, int y, int width, int height, Supplier<ModuleCrafter> moduleCrafter, Consumer<ModuleCrafter> moduleCrafterConsumer, Supplier<SlotDisplay> slotDisplay, Supplier<SmithDisplay> smithDisplay, Consumer<InteractAbleWidget> remover, Consumer<InteractAbleWidget> adder) {
        super(x, y, width, height, Text.empty());
        this.moduleCrafter = moduleCrafter;
        this.moduleCrafterConsumer = moduleCrafterConsumer;
        this.slotDisplay = slotDisplay;
        this.smithDisplay = smithDisplay;
        this.remover = remover;
        this.adder = adder;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            int offset = isEnabled ? minimizeAmount : -minimizeAmount;
            ModuleCrafter crafter = moduleCrafter.get();
            crafter = new ModuleCrafter(crafter.getX(), crafter.getY(), crafter.getWidth(), crafter.getHeight() + offset, crafter);
            moduleCrafterConsumer.accept(crafter);
            if (isEnabled) {
                remover.accept(slotDisplay.get());
                remover.accept(smithDisplay.get());
            } else {
                adder.accept(slotDisplay.get());
                adder.accept(smithDisplay.get());
            }

            isEnabled = !isEnabled;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(getX(), getY(), getX()+getWidth(), getY()+getHeight(), 10, new Color(255, 0, 0, 255).argb());
        super.render(drawContext, mouseX, mouseY, delta);
    }
}
