package smartin.miapi.client.gui.crafting.crafter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.cache.ModularItemCache;
import smartin.miapi.item.modular.properties.ModuleProperty;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.Map;
import java.util.function.Consumer;

public class EditDevView extends InteractAbleWidget {

    SimpleButton backButton;
    SimpleButton craftButton;

    public EditDevView(int x, int y, int width, int height, SlotProperty.ModuleSlot slot, ItemModule.ModuleInstance moduleInstance, ItemStack stack, Consumer<ItemStack> edited) {
        super(x, y, width, height, Text.empty());
        backButton = new SimpleButton(this.x + 10, this.y + this.height - 10, 40, 10, Text.literal("Back"), null, (a) -> {

        });
        MutableText text = Text.literal(moduleInstance.moduleData.get("properties")).copy();
        TextFieldWidget textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 5, y + 10, this.width - 10, 20, text);
        textFieldWidget.setMaxLength(Integer.MAX_VALUE);
        textFieldWidget.setEditable(true);

        ScrollingTextWidget error = new ScrollingTextWidget(x + 5, y + 40, this.width - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 0, 0));
        addChild(error);

        craftButton = new SimpleButton(this.x + this.width - 50, this.y + this.height - 10, 40, 10, Text.literal("Apply"), null, (a) -> {
            String raw = textFieldWidget.getText();
            Miapi.LOGGER.info("attempting writing runtime Properties");
            Miapi.LOGGER.info(raw);
            //validateString
            try {
                boolean success = true;
                if (raw != null) {
                    JsonObject moduleJson = Miapi.gson.fromJson(raw, JsonObject.class);
                    if (moduleJson != null) {
                        for (Map.Entry<String, JsonElement> stringJsonElementEntry : moduleJson.entrySet()) {
                            ModuleProperty property = Miapi.modulePropertyRegistry.get(stringJsonElementEntry.getKey());
                            try {
                                property.load(moduleInstance.module.getName(), stringJsonElementEntry.getValue());
                            } catch (Exception e) {
                                error.setText(Text.of(e.getMessage()));
                                error.textColor = ColorHelper.Argb.getArgb(255, 255, 0, 0);
                                e.printStackTrace();
                                success = false;
                            }
                        }
                    }
                }
                if (success) {
                    Miapi.LOGGER.info("success");
                    moduleInstance.moduleData.put("properties", raw);
                    slot.parent.subModules.put(slot.id,moduleInstance);
                    Miapi.LOGGER.info(moduleInstance.toString());
                    stack.getOrCreateNbt().putString("modules", moduleInstance.getRoot().toString());
                    ItemStack stack1 = stack.copy();
                    stack1.getOrCreateNbt().remove(ModularItemCache.CACHE_KEY);
                    ScreenHandler screenHandler = Miapi.server.getPlayerManager().getPlayerList().get(0).currentScreenHandler;
                    if(screenHandler instanceof CraftingScreenHandler screenHandler1){
                        screenHandler1.setItem(stack1.copy());
                    }
                    ModularItemCache.discardCache();
                    error.setText(Text.of("success"));
                    error.textColor = ColorHelper.Argb.getArgb(255, 0, 255, 0);
                }
            } catch (Exception all) {
                error.setText(Text.of(all.getMessage()));
                error.textColor = ColorHelper.Argb.getArgb(255, 255, 0, 0);
                all.printStackTrace();
            }
        });

        addChild(backButton);
        addChild(textFieldWidget);
        addChild(craftButton);
    }
}
