package smartin.miapi.item.modular.properties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.ItemModule;
import smartin.miapi.item.modular.StatResolver;
import smartin.miapi.item.modular.properties.crafting.CraftingProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialProperty extends CraftingProperty {

    public static String key = "material";

    public static ModuleProperty materialProperty;

    public static Map<String, JsonElement> materialMap = new HashMap<>();

    public MaterialProperty() {
        materialProperty = this;
        StatResolver.registerResolver(key, new StatResolver.Resolver() {
            @Override
            public double resolveDouble(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getKeyedProperties().get(key);
                try {
                    if (jsonData != null) {
                        jsonData = materialMap.get(jsonData.getAsString());
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsDouble();
                            }
                        }
                    }
                } catch (Exception suppressed) {

                }
                return 0;
            }

            @Override
            public String resolveString(String data, ItemModule.ModuleInstance instance) {
                JsonElement jsonData = instance.getProperties().get(materialProperty);
                try {
                    if (jsonData != null) {
                        jsonData = materialMap.get(jsonData.getAsString());
                        if (jsonData != null) {
                            String[] keys = data.split("\\.");
                            for (String key : keys) {
                                jsonData = jsonData.getAsJsonObject().get(key);
                                if (jsonData == null) {
                                    break;
                                }
                            }
                            if (jsonData != null) {
                                return jsonData.getAsString();
                            }
                        }
                    }
                } catch (Exception suppressed) {

                }
                return "";
            }
        });
        ReloadEvents.DataPackLoader.subscribe((path, data) -> {
            if (path.contains("material")) {
                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(data).getAsJsonObject();
                materialMap.put(obj.get("key").getAsString(), obj);
            }
        });
    }

    public List<Vec2f> getSlotPositions(){
        List<Vec2f> test = new ArrayList<>();
        test.add(new Vec2f(5,5));
        return test;
    }

    public InteractAbleWidget createGui(){
        return new test(0,0,10,10);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }

    @Nullable
    public static String getMaterial(ItemModule.ModuleInstance instance){
        JsonElement element = instance.getProperties().get(materialProperty);
        if(element!=null){
            return element.getAsString();
        }
        return null;
    }

    public static void setMaterial(ItemModule.ModuleInstance instance,String material){
        String propertyString = instance.moduleData.computeIfAbsent("properties",(key)->{
            return "{material:empty}";
        });
        JsonObject moduleJson = Miapi.gson.fromJson( propertyString, JsonObject.class);
        moduleJson.addProperty("material",material);
        instance.moduleData.put("properties",Miapi.gson.toJson(moduleJson));
    }

    @Override
    public ItemStack preview(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory, PacketByteBuf buf) {
        return crafting;
    }

    @Override
    public List<ItemStack> performCraftAction(ItemStack old, ItemStack crafting, PlayerEntity player, ItemModule.ModuleInstance newModule, ItemModule module, List<ItemStack> inventory,PacketByteBuf buf) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(this.preview(old, crafting, player, newModule, module, inventory,buf));
        stacks.addAll(inventory);
        stacks.set(1,ItemStack.EMPTY);
        return stacks;
    }

    public class test extends InteractAbleWidget {

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
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
            drawSquareBorder(matrices,x,y,width,height,4, ColorHelper.Argb.getArgb(255,255,255,255));
        }
    }
}
