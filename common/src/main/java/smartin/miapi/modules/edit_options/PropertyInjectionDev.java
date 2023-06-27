package smartin.miapi.modules.edit_options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreenHandler;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.SlotProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class PropertyInjectionDev implements EditOption {
    @Override
    public ItemStack execute(PacketByteBuf buffer, ItemStack stack, ItemModule.ModuleInstance moduleInstance) {
        String raw = buffer.readString();
        moduleInstance.moduleData.put("properties", raw);
        stack.getOrCreateNbt().putString("modules", moduleInstance.getRoot().toString());
        ItemStack stack1 = stack.copy();
        stack1.getOrCreateNbt().remove(ModularItemCache.CACHE_KEY);
        ModularItemCache.discardCache();
        return stack1;
    }

    @Override
    public boolean isVisible(ItemStack stack, ItemModule.ModuleInstance instance) {

        return true;
    }

    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance instance, Consumer<PacketByteBuf> craft, Consumer<PacketByteBuf> preview, Consumer<Objects> back) {
        return new EditDevView(x, y, width, height, stack, instance, craft, back);
    }

    public class EditDevView extends InteractAbleWidget {

        public EditDevView(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance moduleInstance, Consumer<PacketByteBuf> craft, Consumer<Objects> back) {
            super(x, y, width, height, Text.empty());
            SimpleButton<Objects> backButton = new SimpleButton(this.getX() + 10, this.getY() + this.height - 10, 40, 10, Text.literal("Back"), null, back);
            MutableText text = Text.literal(moduleInstance.moduleData.get("properties")).copy();
            TextFieldWidget textFieldWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, x + 5, y + 10, this.width - 10, 20, text);
            textFieldWidget.setMaxLength(Integer.MAX_VALUE);
            textFieldWidget.setEditable(true);

            ScrollingTextWidget error = new ScrollingTextWidget(x + 5, y + 40, this.width - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 0, 0));
            addChild(error);

            SimpleButton<Objects> craftButton = new SimpleButton<>(this.getX() + this.width - 50, this.getY() + this.height - 10, 40, 10, Text.literal("Apply"), null, (a) -> {
                String raw = textFieldWidget.getText();
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
                        PacketByteBuf buf = Networking.createBuffer();
                        buf.writeString(raw);
                        craft.accept(buf);
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

}
