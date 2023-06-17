package smartin.miapi.modules.properties;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.ArrayList;
import java.util.List;

public class AllowedMaterial implements CraftingProperty, ModuleProperty {
    public static final String KEY = "allowedMaterial";

    public List<Vec2f> getSlotPositions() {
        List<Vec2f> test = new ArrayList<>();
        test.add(new Vec2f(160, 130));
        return test;
    }

    @Override
    public float getPriority() {
        return -1;
    }

    public InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction action) {
        return new MaterialCraftingWidget(x, y, width, height, action);
    }

    public Text getWarning() {
        return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.material");
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        if (element != null) {
            AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
            MaterialProperty.Material material = MaterialProperty.getMaterial(input);
            if (material != null) {
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                if (isAllowed) {
                    return input.getCount() * material.getValueOfItem(input) >= json.cost;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        if (element != null) {
            MaterialProperty.Material material = MaterialProperty.getMaterial(input);
            if (material != null) {
                MaterialProperty.setMaterial(newModule, material.key);
            }
        }
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        List<ItemStack> results = new ArrayList<>();
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
        MaterialProperty.Material material = MaterialProperty.getMaterial(input);
        assert material != null;
        int newCount = (int) (input.getCount() - Math.ceil(json.cost / material.getValueOfItem(input)));
        input.setCount(newCount);
        MaterialProperty.setMaterial(newModule, material.key);
        results.add(crafting);
        results.add(input);
        return results;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    static class MaterialCraftingWidget extends InteractAbleWidget {
        private final int startX;
        private final int startY;
        private Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/material_background.png");
        private CraftAction action;
        private TransformableWidget headerHolder;
        private ScrollingTextWidget header;
        private MultiLineTextWidget description;

        /**
         * This is a Widget build to support Children and parse the events down to them.
         * Best use in conjunction with the ParentHandledScreen as it also handles Children correct,
         * unlike the base vanilla classes.
         * If you choose to handle some Events yourself and want to support Children yourself, you need to call the correct
         * super method or handle the children yourself
         *
         * @param x      the X Position
         * @param y      the y Position
         * @param width  the width
         * @param height the height
         */
        public MaterialCraftingWidget(int x, int y, int width, int height, CraftAction action) {
            super(x, y, width, height, Text.literal("Test"));
            startX = x + 5;
            startY = y + 5;
            this.action = action;
            Miapi.LOGGER.warn("WIDTH " + width + " height " + height);
            Miapi.LOGGER.warn("WIDTH X" + x + " height Y" + y);

            ItemModule.ModuleInstance moduleInstance = new ItemModule.ModuleInstance(action.toAdd);
            Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
            Text descriptionText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName() + ".description", moduleInstance);

            float headerScale = 2;

            headerHolder = new TransformableWidget(x, y, width, height, headerScale);
            addChild(headerHolder);


            header = new ScrollingTextWidget((int) ((this.x + 5) / headerScale), (int) (this.y / headerScale), (int) ((this.width - 10) / headerScale), displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            headerHolder.addChild(header);
            description = new MultiLineTextWidget(x + 5, y + 30, width - 10, height - 40, descriptionText);
            addChild(description);
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            //RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            int textureSize = 30;
            int textureOffset = 0;

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, this.width, this.height);

            //drawSquareBorder(matrices, x, y - 30, width, height, 4, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            super.render(matrices, mouseX, mouseY, delta);
        }
    }

    static class AllowedMaterialJson {
        public List<String> allowedMaterials;
        public float cost;
    }
}
