package smartin.miapi.modules.edit_options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.ClickAbleTextWidget;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.SimpleButton;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.config.MiapiConfig;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.cache.ModularItemCache;
import smartin.miapi.modules.properties.util.ModuleProperty;
import smartin.miapi.network.Networking;
import smartin.miapi.registries.RegistryInventory;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PropertyInjectionDev implements EditOption {
    @Override
    public ItemStack preview(PacketByteBuf buffer, EditContext context) {
        String raw = buffer.readString();
        assert context.getInstance() != null;
        context.getInstance().moduleData.put("properties", raw);
        ItemStack stack1 = context.getItemstack().copy();
        context.getInstance().getRoot().writeToItem(stack1);
        ModularItemCache.discardCache();
        return stack1;
    }

    @Override
    public boolean isVisible(EditContext context) {
        return MiapiConfig.INSTANCE.server.other.developmentMode && context.getInstance() != null;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, EditContext context) {
        return new EditDevView(x, y, width, height, context.getItemstack(), context.getInstance(), context::craft);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getIconGui(int x, int y, int width, int height, Consumer<EditOption> select, Supplier<EditOption> getSelected) {
        return new EditOptionIcon(x, y, width, height, select, getSelected, CraftingScreen.BACKGROUND_TEXTURE, 339, 25 + 28 * 2, 512, 512, this);
    }

    @Environment(EnvType.CLIENT)
    public static class EditDevView extends InteractAbleWidget {

        public EditDevView(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance moduleInstance, Consumer<PacketByteBuf> craft) {
            super(x, y, width, height, Text.empty());
            MutableText text = Text.literal(moduleInstance.moduleData.get("properties")).copy();
            TextFieldWidget textFieldWidget = new ClickAbleTextWidget(MinecraftClient.getInstance().textRenderer, x + 5, y + 10, this.width - 10, 20, text);
            textFieldWidget.setMaxLength(Integer.MAX_VALUE);
            textFieldWidget.setEditable(true);

            ScrollingTextWidget error = new ScrollingTextWidget(x + 5, y + 40, this.width - 10, Text.empty(), ColorHelper.Argb.getArgb(255, 255, 0, 0));
            addChild(error);

            SimpleButton<Objects> craftButton = new SimpleButton<>(this.getX() + this.width - 41, this.getY() + this.height - 11, 40, 10, Text.literal("Apply"), null, (a) -> {
                String raw = textFieldWidget.getText();
                try {
                    boolean success = true;
                    if (raw != null) {
                        JsonObject moduleJson = Miapi.gson.fromJson(raw, JsonObject.class);
                        if (moduleJson != null) {
                            for (Map.Entry<String, JsonElement> stringJsonElementEntry : moduleJson.entrySet()) {
                                ModuleProperty property = RegistryInventory.moduleProperties.get(stringJsonElementEntry.getKey());
                                try {
                                    assert property != null;
                                    property.load(moduleInstance.module.getName(), stringJsonElementEntry.getValue(), true);
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
            addChild(textFieldWidget);
            addChild(craftButton);
        }
    }

}
