package smartin.miapi.modules.edit_options.skins;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.datapack.ReloadEvents;
import smartin.miapi.item.modular.PropertyResolver;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.modules.edit_options.EditOption;
import smartin.miapi.modules.edit_options.skins.gui.SkinGui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class SkinOptions implements EditOption {

    public static Map<ItemModule, Map<String, Skin>> skins = new HashMap<>();
    public static Map<String, SkinTab> tabMap = new HashMap<>();
    public static SkinTab defaultTab = new SkinTab();

    public SkinOptions() {
        defaultTab = SkinTab.fromJson(null);
        PropertyResolver.propertyProviderRegistry.register("skin", (moduleInstance, oldMap) -> {
            if (moduleInstance != null) {
                String skinKey = moduleInstance.moduleData.get("skin");
                Map<String, Skin> moduleSkins = skins.get(moduleInstance.module);
                if (skinKey != null && moduleSkins != null && moduleSkins.containsKey(skinKey)) {
                    oldMap.putAll(moduleSkins.get(skinKey).properties);
                }
            }
            return oldMap;
        });
        Miapi.registerReloadHandler(ReloadEvents.END, "skins/module", (isClient, path, data) -> {
            load(data);
        });
        Miapi.registerReloadHandler(ReloadEvents.END, "skins/tab", (isClient, path, data) -> {
            loadTabData(data);
        });

        // old code below, not deleted for reference reasons- Smartin feel free to delete if you want i guess
        /*ReloadEvents.END.subscribe((isClient -> {
            skins.clear();
            ReloadEvents.DATA_PACKS.forEach((path, data) -> {
                if (path.startsWith("skins/module")) {
                    load(data);
                }
                if (path.startsWith("skins/tab")) {
                    loadTabData(data);
                }
            });
        }));*/
    }

    public static SkinTab getTag(String path) {
        return tabMap.getOrDefault(path, defaultTab);
    }

    public static void load(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        Skin skin = Skin.fromJson(element);

        Map<String, Skin> skinMap = skins.computeIfAbsent(skin.module, (module) -> {
            return new HashMap<>();
        });

        skinMap.put(skin.path, skin);
    }

    public static void loadTabData(String data) {
        JsonObject element = Miapi.gson.fromJson(data, JsonObject.class);
        SkinTab tab = SkinTab.fromJson(element);
        tabMap.put(tab.path, tab);
    }

    @Override
    public ItemStack execute(PacketByteBuf buffer, ItemStack stack, ItemModule.ModuleInstance instance) {
        String skin = buffer.readString();
        instance.moduleData.put("skin", skin);
        instance.getRoot().writeToItem(stack);
        return stack;
    }

    @Override
    public boolean isVisible(ItemStack stack, ItemModule.ModuleInstance instance) {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public InteractAbleWidget getGui(int x, int y, int width, int height, ItemStack stack, ItemModule.ModuleInstance instance, Consumer<PacketByteBuf> craft, Consumer<PacketByteBuf> preview, Consumer<Objects> back) {
        return new SkinGui(x, y, width, height, stack, instance, craft, preview, back);
    }
}
