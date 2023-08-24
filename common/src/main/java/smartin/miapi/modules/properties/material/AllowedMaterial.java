package smartin.miapi.modules.properties.material;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.Miapi;
import smartin.miapi.blocks.ModularWorkBenchEntity;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.client.gui.MultiLineTextWidget;
import smartin.miapi.client.gui.ScrollingTextWidget;
import smartin.miapi.client.gui.TransformableWidget;
import smartin.miapi.craft.CraftAction;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.properties.util.CraftingProperty;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This property manages the allowed Materials for a module
 */
public class AllowedMaterial implements CraftingProperty, ModuleProperty {
    public static final String KEY = "allowedMaterial";
    public double materialCostClient = 0.0f;
    public double materialRequirementClient = 0.0f;
    public boolean wrongMaterial = false;

    public List<Vec2f> getSlotPositions() {
        List<Vec2f> test = new ArrayList<>();
        test.add(new Vec2f(160, 130));
        return test;
    }

    @Override
    public float getPriority() {
        return -1;
    }


    @Environment(EnvType.CLIENT)
    public InteractAbleWidget createGui(int x, int y, int width, int height, CraftAction action) {
        return new MaterialCraftingWidget(x, y, width, height, action);
    }

    public Text getWarning() {
        if (wrongMaterial) {
            return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.material.wrong");
        }
        return Text.translatable(Miapi.MOD_ID + ".ui.craft.warning.material");
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, ModularWorkBenchEntity bench, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        if (element != null) {
            AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
            Material material = MaterialProperty.getMaterial(input);
            materialRequirementClient = json.cost * crafting.getCount();
            if (material != null) {
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                wrongMaterial = !isAllowed;
                if (isAllowed) {
                    materialCostClient = input.getCount() * material.getValueOfItem(input);
                    return materialCostClient >= materialRequirementClient;
                } else {
                    materialCostClient = 0.0f;
                }
            } else {
                wrongMaterial = false;
                materialCostClient = 0.0f;
            }
        } else {
            wrongMaterial = false;
        }
        return false;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        ItemStack inputCopy = input.copy();
        if (element != null) {
            Material material = MaterialProperty.getMaterial(input);
            if (material != null) {
                AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
                boolean isAllowed = (json.allowedMaterials.stream().anyMatch(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)));
                if (isAllowed) {
                    MaterialProperty.setMaterial(newModule, material.getKey());
                }
            }
        }
        crafting = MaterialInscribeProperty.inscribe(crafting, inputCopy);
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ModularWorkBenchEntity bench, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        List<ItemStack> results = new ArrayList<>();
        JsonElement element = module.getProperties().get(KEY);
        ItemStack input = inventory.get(0);
        ItemStack inputCopy = input.copy();
        AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
        Material material = MaterialProperty.getMaterial(input);
        assert material != null;
        int newCount = (int) (input.getCount() - Math.ceil(json.cost * crafting.getCount() / material.getValueOfItem(input)));
        input.setCount(newCount);
        MaterialProperty.setMaterial(newModule, material.getKey());
        crafting = MaterialInscribeProperty.inscribe(crafting, inputCopy);
        results.add(crafting);
        results.add(input);
        return results;
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Environment(EnvType.CLIENT)
    class MaterialCraftingWidget extends InteractAbleWidget {
        private final Identifier texture = new Identifier(Miapi.MOD_ID, "textures/gui/crafter/material_background.png");
        private final TransformableWidget headerHolder;
        private final ScrollingTextWidget header;
        private final MultiLineTextWidget description;
        private final ScrollingTextWidget costDescr;
        private final DecimalFormat modifierFormat = Util.make(new DecimalFormat("##.##"), (decimalFormat) -> {
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        });

        public MaterialCraftingWidget(int x, int y, int width, int height, CraftAction action) {
            super(x, y, width, height, Text.literal("Test"));

            ItemModule.ModuleInstance moduleInstance = new ItemModule.ModuleInstance(action.toAdd);
            Text displayText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName(), moduleInstance);
            Text descriptionText = StatResolver.translateAndResolve(Miapi.MOD_ID + ".module." + moduleInstance.module.getName() + ".description", moduleInstance);

            float headerScale = 2;

            headerHolder = new TransformableWidget(x, y, width, height, headerScale);
            addChild(headerHolder);


            header = new ScrollingTextWidget((int) ((this.getX() + 5) / headerScale), (int) (this.getY() / headerScale), (int) ((this.width - 10) / headerScale), displayText, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            headerHolder.addChild(header);
            description = new MultiLineTextWidget(x + 5, y + 30, width - 10, height - 40, descriptionText);
            costDescr = new ScrollingTextWidget(x + this.width - 80, y + this.height - 8, 78, Text.empty());
            costDescr.setOrientation(ScrollingTextWidget.Orientation.RIGHT);
            costDescr.textColor = ColorHelper.Argb.getArgb(255, 225, 225, 225);
            addChild(description);
            addChild(costDescr);
        }

        public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
            RenderSystem.enableDepthTest();

            int textureOffset = 0;

            if (materialCostClient < materialRequirementClient) {
                costDescr.textColor = ColorHelper.Argb.getArgb(255, 225, 225, 125);
            } else {
                costDescr.textColor = ColorHelper.Argb.getArgb(255, 125, 225, 125);
            }

            costDescr.setText(Text.literal(modifierFormat.format(materialCostClient) + "/" + modifierFormat.format(materialRequirementClient)));

            drawContext.drawTexture(texture, getX(), getY(), 0, textureOffset, 0, this.width, this.height, this.width, this.height);

            super.render(drawContext, mouseX, mouseY, delta);
        }
    }

    static class AllowedMaterialJson {
        public List<String> allowedMaterials;
        public float cost;
    }
}
