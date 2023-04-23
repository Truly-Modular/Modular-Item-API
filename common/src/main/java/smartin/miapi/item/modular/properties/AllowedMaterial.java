package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ItemModule;

import java.util.ArrayList;
import java.util.List;

public class AllowedMaterial extends CraftingProperty {

    public static final String key = "allowedMaterial";

    public List<Vec2f> getSlotPositions() {
        List<Vec2f> test = new ArrayList<>();
        test.add(new Vec2f(5, 5));
        return test;
    }

    @Override
    public float getPriority() {
        return -1;
    }

    public InteractAbleWidget createGui(int x, int y, int width, int height) {
        return new test(x, y, width, height);
    }

    @Override
    public boolean canPerform(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        //AllowedMaterialJson json = Miapi.gson.fromJson()
        JsonElement element = module.getProperties().get(key);
        ItemStack input = inventory.get(0);
        if (element != null) {
            AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
            MaterialProperty.Material material = MaterialProperty.getMaterial(input);
            if (material != null) {
                boolean isAllowed = (json.allowedMaterials.stream().filter(allowedMaterial ->
                        material.getGroups().contains(allowedMaterial)
                ).count() > 0);
                if (isAllowed || true) {
                    if (input.getCount() * material.getValueOfItem(input) >= json.cost) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        JsonElement element = module.getProperties().get(key);
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
        JsonElement element = module.getProperties().get(key);
        ItemStack input = inventory.get(0);
        AllowedMaterialJson json = Miapi.gson.fromJson(element, AllowedMaterialJson.class);
        MaterialProperty.Material material = MaterialProperty.getMaterial(input);
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

    public class test extends InteractAbleWidget {
        private final int startX;
        private final int startY;
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
        public test(int x, int y, int width, int height) {
            super(x, y, width, height, Text.literal("Test"));
            startX = x+5;
            startY = y+5;
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            drawSquareBorder(matrices, x, y, width, height, 4, ColorHelper.Argb.getArgb(255, 255, 255, 255));
            drawSquareBorder(matrices, x+5, y+5, 9, 9, 4, ColorHelper.Argb.getArgb(255, 0, 0, 0));
            drawSquareBorder(matrices, startX, startY, 9, 9, 4, ColorHelper.Argb.getArgb(255, 255, 255, 0));
        }
    }

    private class AllowedMaterialJson {
        public List<String> allowedMaterials;
        public float cost;
    }
}
